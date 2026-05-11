package io.nicheblog.dreamdiary.feature.chat.service;

import io.nicheblog.dreamdiary.feature.chat.client.OllamaClient;
import io.nicheblog.dreamdiary.feature.chat.entity.ChatSessionEntity;
import io.nicheblog.dreamdiary.feature.chat.model.ChatMessageDto;
import io.nicheblog.dreamdiary.feature.journal.embedding.entity.JournalEntryEmbeddingEntity;
import io.nicheblog.dreamdiary.feature.journal.embedding.service.JournalEntryEmbeddingSearchService;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ChatAIService
 * <pre>
 *  사용자 메시지 저장, 대화 맥락 구성, AI 응답 생성, WebSocket 브로드캐스트를 묶어 처리하는 서비스.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class ChatAIService {

    /** RAG 검색에서 가져올 최대 저널 엔트리 수 */
    private static final int RAG_TOP_K = 5;
    /** RAG 컨텍스트에 포함할 엔트리당 최대 텍스트 길이 */
    private static final int RAG_TEXT_MAX_LENGTH = 300;

    private final ChatMessageService chatMessageService;
    private final ChatSessionService chatSessionService;
    private final ChatSettingService chatSettingService;
    private final OllamaClient ollamaClient;
    private final JournalEntryEmbeddingSearchService embeddingSearchService;
    private final SimpMessagingTemplate messagingTemplate;

    /** 세션별 응답 취소 플래그 */
    private final Map<Integer, AtomicBoolean> cancelFlags = new ConcurrentHashMap<>();

    /**
     * 사용자 메시지를 저장한 뒤 최근 대화 맥락을 포함해 AI 응답을 생성하고 세션 구독자에게 전송한다.
     *
     * @param sessionId 메시지가 속한 채팅 세션 ID
     * @param message 사용자 입력 메시지
     * @throws Exception 세션 검증, 메시지 저장, AI 호출 중 예외가 발생한 경우
     */
    public void processChat(final Integer sessionId, final String message) throws Exception {
        cancelFlags.computeIfAbsent(sessionId, k -> new AtomicBoolean(false)).set(false);

        final ChatSessionEntity session = chatSessionService.getMySessionEntity(sessionId);

        // 1. 사용자 메시지 저장
        final ChatMessageDto userMessage = ChatMessageDto.builder()
                        .sessionId(sessionId)
                        .seq(chatMessageService.getNextSeq(sessionId))
                        .role("USER")
                        .content(message)
                        .build();
        final ServiceResponse userResult = chatMessageService.regist(userMessage);
        chatSessionService.touchAfterMessage(sessionId, message);

        // 2. 사용자 메시지 broadcast
        messagingTemplate.convertAndSend(
                "/topic/chat/session/" + sessionId,
                AjaxResponse.fromResponseWithObj(
                        userResult,
                        MessageUtils.RSLT_SUCCESS
                )
        );

        // 3. AI 응답 생성 (RAG 컨텍스트 주입)
        final int recentMessageLimit = chatSettingService.getMyRecentMessageLimit();
        final String ragContext = buildRagContext(message);
        final String systemPrompt = buildSystemPromptWithRag(
                StringUtils.defaultIfBlank(session.getSystemPrompt(), chatSessionService.getDefaultSystemPrompt()),
                ragContext
        );
        final String rawResponse = ollamaClient.chat(
                        systemPrompt,
                        chatMessageService.getRecentContextMessages(sessionId, recentMessageLimit)
                );

        // 취소 요청이 들어왔으면 저장/broadcast 없이 종료
        if (isCancelled(sessionId)) {
            log.info("AI response cancelled. sessionId={}", sessionId);
            cancelFlags.remove(sessionId);
            return;
        }

        final String aiResponse = stripMarkdownBold(rawResponse);

        // 4. AI 메시지 저장
        final ChatMessageDto aiMessage = ChatMessageDto.builder()
                        .sessionId(sessionId)
                        .seq(chatMessageService.getNextSeq(sessionId))
                        .role("ASSISTANT")
                        .title("Dreamdiary AI")
                        .content(aiResponse)
                        .build();
        final ServiceResponse aiResult = chatMessageService.regist(aiMessage);
        chatSessionService.touchAfterMessage(sessionId, message);

        // 5. AI 메시지 broadcast
        messagingTemplate.convertAndSend(
                "/topic/chat/session/" + sessionId,
                AjaxResponse.fromResponseWithObj(
                        aiResult,
                        MessageUtils.RSLT_SUCCESS
                )
        );

        cancelFlags.remove(sessionId);
    }

    /**
     * 세션의 AI 응답 생성을 취소 요청한다.
     *
     * <p>Ollama 호출이 완료되기 전에 플래그를 세팅하면,
     * 응답이 도착해도 저장과 broadcast를 건너뛴다.</p>
     *
     * @param sessionId 취소할 채팅 세션 ID
     */
    public void cancelChat(final Integer sessionId) {
        if (sessionId == null) return;
        cancelFlags.computeIfAbsent(sessionId, k -> new AtomicBoolean(false)).set(true);
        log.info("AI response cancel requested. sessionId={}", sessionId);
    }

    /**
     * 취소 플래그가 세팅되어 있는지 확인한다.
     */
    private boolean isCancelled(final Integer sessionId) {
        final AtomicBoolean flag = cancelFlags.get(sessionId);
        return flag != null && flag.get();
    }

    /**
     * 사용자 메시지와 의미상 유사한 저널 기록을 검색해 RAG 컨텍스트 문자열을 생성합니다.
     *
     * <p>Ollama 임베딩 호출 실패 시 null을 반환해 채팅이 중단되지 않도록 합니다.</p>
     *
     * @param queryText 검색할 사용자 메시지
     * @return 포맷팅된 RAG 컨텍스트 문자열, 결과가 없거나 오류 발생 시 null
     */
    private String buildRagContext(final String queryText) {
        try {
            final List<JournalEntryEmbeddingEntity> results = embeddingSearchService.search(queryText, RAG_TOP_K);
            if (results.isEmpty()) return null;

            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < results.size(); i++) {
                sb.append("[").append(i + 1).append("]\n");
                sb.append(StringUtils.abbreviate(StringUtils.defaultString(results.get(i).getEmbeddingText()), RAG_TEXT_MAX_LENGTH));
                sb.append("\n\n");
            }
            return sb.toString().trim();
        } catch (final Exception e) {
            log.warn("RAG context search failed, proceeding without context. error={}", e.getMessage());
            return null;
        }
    }

    /**
     * 기본 시스템 프롬프트에 RAG 컨텍스트를 추가합니다.
     *
     * @param basePrompt 세션 또는 기본 시스템 프롬프트
     * @param ragContext 저널 검색 결과 컨텍스트
     * @return RAG 컨텍스트가 포함된 최종 시스템 프롬프트
     */
    private String buildSystemPromptWithRag(final String basePrompt, final String ragContext) {
        if (StringUtils.isBlank(ragContext)) return basePrompt;
        return basePrompt
                + "\n\n## 참고할 저널 기록\n"
                + "아래는 현재 질문과 관련성이 높은 나의 저널 기록입니다. 답변 시 참고하세요.\n\n"
                + ragContext;
    }

    /**
     * AI 응답에서 마크다운 굵은글씨 기호(**text**, __text__)를 제거합니다.
     *
     * @param text 원본 AI 응답 텍스트
     * @return 굵은글씨 기호가 제거된 텍스트
     */
    private String stripMarkdownBold(final String text) {
        if (text == null) return null;
        return text.replaceAll("\\*\\*(.+?)\\*\\*", "$1")
                   .replaceAll("__(.+?)__", "$1");
    }
}