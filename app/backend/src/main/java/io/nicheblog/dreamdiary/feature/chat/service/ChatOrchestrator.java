package io.nicheblog.dreamdiary.feature.chat.service;

import io.nicheblog.dreamdiary.feature.ai.client.OllamaClient;
import io.nicheblog.dreamdiary.feature.ai.guard.ResponseGuardService;
import io.nicheblog.dreamdiary.feature.ai.prompt.IntentPromptResolver;
import io.nicheblog.dreamdiary.feature.ai.prompt.SystemPromptBuilder;
import io.nicheblog.dreamdiary.feature.ai.person.PersonFocus;
import io.nicheblog.dreamdiary.feature.ai.person.PersonQueryClassifier;
import io.nicheblog.dreamdiary.feature.ai.person.PersonSynthesisHybridService;
import io.nicheblog.dreamdiary.feature.ai.person.PersonSynthesisResult;
import io.nicheblog.dreamdiary.feature.ai.model.AiChatMessage;
import io.nicheblog.dreamdiary.feature.chat.entity.ChatSessionEntity;
import io.nicheblog.dreamdiary.feature.chat.model.ChatMessageDto;
import io.nicheblog.dreamdiary.feature.ai.rag.RagContext;
import io.nicheblog.dreamdiary.feature.ai.rag.RagContextService;
import io.nicheblog.dreamdiary.feature.ai.rag.RagIntent;
import io.nicheblog.dreamdiary.feature.ai.rag.RagSearchLimits;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.RagSearchResult;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import io.nicheblog.dreamdiary.feature.chat.ws.ChatWebSocketSender;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * ChatOrchestrator
 * <pre>
 *  채팅 채널 오케스트레이터. 사용자 메시지 저장, RAG 컨텍스트 위임({@link RagContextService}), AI 응답 생성, WebSocket 브로드캐스트를 묶어 처리한다.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class ChatOrchestrator {

    /** RAG 검색에서 가져올 최대 저널 엔트리 수 (chat_setting 기본값) */
    private static final int RAG_TOP_K = 5;
    /** 요약형 RAG 검색에서 가져올 최대 저널 엔트리 수 */
    private static final int RAG_SUMMARY_TOP_K = 12;
    /** 통섭형 RAG 검색에서 가져올 최대 저널 엔트리 수 */
    private static final int RAG_SYNTHESIS_TOP_K = 25;
    /** LOOKUP/SUMMARY 벡터 기본 최소 점수 (관리자 {@code chat_setting.rag_min_score} 기본값). */
    private static final double RAG_MIN_SCORE = 0.35D;
    /** SYNTHESIS 벡터 기본 최소 점수 (관리자 {@code chat_setting.rag_synthesis_min_score} 기본값). */
    private static final double RAG_SYNTHESIS_MIN_SCORE = 0.25D;
    /** person-stance(태도) 질문 tag-only RAG에서 가져올 최대 저널 엔트리 수 */
    private static final int PERSON_STANCE_RAG_TOP_K = 50;
    private final ChatMessageService chatMessageService;
    private final ChatSessionService chatSessionService;
    private final ChatSettingService chatSettingService;
    private final OllamaClient ollamaClient;
    private final RagContextService ragContextService;
    private final PersonSynthesisHybridService personSynthesisHybridService;
    private final ResponseGuardService responseGuardService;
    private final SystemPromptBuilder systemPromptBuilder;
    private final IntentPromptResolver intentPromptResolver;
    private final ChatWebSocketSender chatWebSocketSender;
    private final RagMetadataJsonBuilder ragMetadataJsonBuilder;

    /** 세션별 응답 취소 플래그 */
    private final Map<Integer, AtomicBoolean> cancelFlags = new ConcurrentHashMap<>();

    /** chat.ai.* 카탈로그 메시지를 현재 locale 로 조회한다. */
    private String chatMsg(final String key, final Object... args) {
        return MessageUtils.getMessage(key, args);
    }

    /** LLM 이 언어 규칙을 어긴 경우 1회 재시도할 때 추가하는 지시문 (locale 카탈로그). */
    private String languageRetryPrompt() {
        return responseGuardService.languageRetryPrompt();
    }


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
        chatWebSocketSender.broadcastMessage(
                sessionId,
                AjaxResponse.fromResponseWithObj(
                        userResult,
                        MessageUtils.getMessage("common.result.success")
                )
        );

        // 3. AI 응답 생성 (RAG 컨텍스트 주입)
        final int recentMessageLimit = chatSettingService.getMyRecentMessageLimit();
        chatWebSocketSender.broadcastProgress(sessionId, "SEARCHING");
        final RagContext ragContext = buildRagContext(message);

        if (isCancelled(sessionId)) {
            log.info("AI response cancelled. sessionId={}", sessionId);
            cancelFlags.remove(sessionId);
            return;
        }

        chatWebSocketSender.broadcastProgress(sessionId, "GENERATING");
        final String strippedResponse;
        final String responseMode;
        String guardDetail = null;
        String retryGuardDetail = null;
        if (shouldUseRulePrimaryPersonSynthesisResponse(ragContext, message)) {
            log.info("AI person synthesis hybrid. sessionId={}, stance={}, appearance={}",
                    sessionId, isPersonAttitudeQuery(message), isPersonAppearanceQuery(message));
            final ResolvedChatResponse resolved = resolvePersonSynthesisHybridResponse(
                    sessionId,
                    message,
                    ragContext,
                    recentMessageLimit
            );
            if (isCancelled(sessionId)) {
                log.info("AI response cancelled. sessionId={}", sessionId);
                cancelFlags.remove(sessionId);
                return;
            }
            strippedResponse = resolved.content();
            responseMode = resolved.responseMode();
            guardDetail = resolved.guardDetail();
            retryGuardDetail = resolved.retryGuardDetail();
        } else {
            final String systemPrompt = buildSystemPromptWithRag(
                    StringUtils.defaultIfBlank(session.getSystemPrompt(), chatSessionService.getDefaultSystemPrompt()),
                    ragContext,
                    message
            );
            final List<ChatMessageDto> contextMessages = sanitizeContextMessages(
                    chatMessageService.getRecentContextMessages(sessionId, recentMessageLimit)
            );
            // 본경로만 스트리밍: 토큰 DELTA를 버블에 미리 보이고, 완성 후 글로벌 가드·저장·완성 broadcast는 기존과 동일.
            // hybrid/retry·language-guard 재시도는 비스트리밍 chat()을 유지한다.
            final AtomicBoolean cancelFlag = cancelFlags.get(sessionId);
            String rawResponse = ollamaClient.chatStream(
                    systemPrompt,
                    toAiChatMessages(contextMessages),
                    delta -> chatWebSocketSender.broadcastDelta(sessionId, delta),
                    cancelFlag
            );
            if (isCancelled(sessionId)) {
                log.info("AI response cancelled during stream. sessionId={}", sessionId);
                cancelFlags.remove(sessionId);
                return;
            }
            if (containsDisallowedHanScript(rawResponse)) {
                log.warn("AI response language guard retry. sessionId={}", sessionId);
                rawResponse = ollamaClient.chat(systemPrompt + languageRetryPrompt(), toAiChatMessages(contextMessages));
            }

            if (isCancelled(sessionId)) {
                log.info("AI response cancelled. sessionId={}", sessionId);
                cancelFlags.remove(sessionId);
                return;
            }

            final ResolvedChatResponse resolved = resolveLlmChatResponse(
                    sessionId,
                    message,
                    ragContext,
                    systemPrompt,
                    contextMessages,
                    stripInternalRecordCitations(rawResponse)
            );
            strippedResponse = resolved.content();
            responseMode = resolved.responseMode();
            guardDetail = resolved.guardDetail();
            retryGuardDetail = resolved.retryGuardDetail();
        }
        final String aiResponse = strippedResponse;

        // 4. AI 메시지 저장
        final ChatMessageDto aiMessage = ChatMessageDto.builder()
                        .sessionId(sessionId)
                        .seq(chatMessageService.getNextSeq(sessionId))
                        .role("ASSISTANT")
                        .title("Dreamdiary AI")
                        .content(aiResponse)
                        .metadataJson(ragMetadataJsonBuilder.buildRagMetadataJson(ragContext, responseMode, guardDetail, retryGuardDetail))
                        .build();
        final ServiceResponse aiResult = chatMessageService.regist(aiMessage);
        chatSessionService.touchAfterMessage(sessionId, message);

        // 5. AI 메시지 broadcast
        chatWebSocketSender.broadcastMessage(
                sessionId,
                AjaxResponse.fromResponseWithObj(
                        aiResult,
                        MessageUtils.getMessage("common.result.success")
                )
        );

        cancelFlags.remove(sessionId);
    }

    /**
     * 세션의 AI 응답 생성을 취소 요청한다.
     *
     * <p>본경로 스트림 중이면 NDJSON 읽기를 중단하고,
     * 완성 전이면 저장·완성 broadcast를 건너뛰다.
     * language-guard 재시도·hybrid 비스트림 호출은 HTTP가 끝날 때까지 대기한 뒤 플래그로 미저장한다.</p>
     *
     * @param sessionId 취소할 채팅 세션 ID
     */
    public void cancelChat(final Integer sessionId) {
        if (sessionId == null) return;
        chatSessionService.getMySessionEntity(sessionId);
        cancelFlags.computeIfAbsent(sessionId, k -> new AtomicBoolean(false)).set(true);
        log.info("AI response cancel requested after ownership validation. sessionId={}", sessionId);
    }

    /**
     * 취소 플래그가 세팅되어 있는지 확인한다.
     */
    private boolean isCancelled(final Integer sessionId) {
        final AtomicBoolean flag = cancelFlags.get(sessionId);
        return flag != null && flag.get();
    }

    /**
     * 사용자 메시지와 의미상 유사한 저널 기록을 검색해 RAG 컨텍스트를 조립한다.
     *
     * <p>검색·텍스트 조립은 {@link RagContextService}에 위임한다. 채널 관리자 RAG 설정만 여기서 매핑한다.</p>
     *
     * @param queryText 검색할 사용자 메시지
     * @return 의도·결과·텍스트·personFocus
     */
    private RagContext buildRagContext(final String queryText) {
        return ragContextService.build(
                queryText,
                toRagSearchLimits(resolveAdminRagSettings()),
                intentPromptResolver.intentClassifyPrompt()
        );
    }

    /**
     * 기본 시스템 프롬프트에 RAG 컨텍스트를 추가합니다.
     *
     * @param basePrompt 세션 또는 기본 시스템 프롬프트
     * @param ragContext 저널 검색 결과 컨텍스트
     * @return RAG 컨텍스트가 포함된 최종 시스템 프롬프트
     */
    private String buildSystemPromptWithRag(
            final String basePrompt,
            final RagContext ragContext,
            final String queryText
    ) {
        return systemPromptBuilder.buildSystemPromptWithRag(
                basePrompt,
                ragContext == null ? null : ragContext.intent(),
                ragContext == null ? null : ragContext.text(),
                queryText
        );
    }

    /**
     * 관리자 RAG 설정을 조회한다. 단위 테스트에서 {@code chatSettingService} null일 때 코드 기본값을 쓰다.
     */
    private ChatSettingService.RagAdminSettings resolveAdminRagSettings() {
        if (chatSettingService == null) {
            return new ChatSettingService.RagAdminSettings(
                    true,
                    RAG_TOP_K,
                    RAG_MIN_SCORE,
                    RAG_SUMMARY_TOP_K,
                    RAG_SYNTHESIS_TOP_K,
                    PERSON_STANCE_RAG_TOP_K,
                    RAG_SYNTHESIS_MIN_SCORE
            );
        }
        return chatSettingService.getAdminRagSettings();
    }

    /**
     * 채널 관리자 RAG 설정을 {@link RagSearchLimits}로 매핑한다.
     *
     * @param settings chat_setting 기반 관리자 값
     * @return ai/rag 검색 한도
     */
    private RagSearchLimits toRagSearchLimits(final ChatSettingService.RagAdminSettings settings) {
        return new RagSearchLimits(
                settings.enabled(),
                settings.topK(),
                settings.minScore(),
                settings.summaryTopK(),
                settings.synthesisTopK(),
                settings.stanceTopK(),
                settings.synthesisMinScore()
        );
    }

    /**
     * 1인칭 태도·자기인식 질문(나는 X를 어떻게 생각/느끼는지)인지 확인합니다.
     *
     * <p>person-meaning(상징·역할 축·등장 방식)과 구분해 Path C 태도 rich-trust 프롬프트·최소 게이트를 태웁니다.
     * (예전에는 PERSON_STANCE_SCAFFOLD·강경 가드를 태웠으나 Option A 수렴으로 제거됨.)
     * {@code 내 대화에서 X는 어떤 느낌으로 등장}처럼 범위+등장 질문은 false입니다.</p>
     */
    private boolean isPersonAttitudeQuery(final String queryText) {
        return PersonQueryClassifier.isPersonAttitudeQuery(queryText);
    }

    /**
     * 기록·대화 속 인물의 등장 방식·느낌·톤을 묻는 질문인지 확인합니다.
     *
     * <p>1인칭 태도 질문과 달리 주어가 인물({@code 지연님은 … 등장})이거나 {@code 내 대화/내 기록} 범위 질문입니다.</p>
     */
    private boolean isPersonAppearanceQuery(final String queryText) {
        return PersonQueryClassifier.isPersonAppearanceQuery(queryText);
    }

    /**
     * SYNTHESIS person 질문은 스냅샷 hybrid(LLM 해석 + rule-primary 폴백) 경로를 탑니다.
     */
    private boolean shouldUseRulePrimaryPersonSynthesisResponse(final RagContext ragContext, final String queryText) {
        if (ragContext == null) return false;
        return personSynthesisHybridService.shouldUsePathC(
                ragContext.personFocus(),
                ragContext.intent(),
                queryText
        );
    }

    /**
     * person SYNTHESIS: 서버 스냅샷 집계 후 LLM 해석 1회를 시도하고, 가드 실패 시 RULE_PRIMARY로 폴백합니다.
     */
    private ResolvedChatResponse resolvePersonSynthesisHybridResponse(
            final Integer sessionId,
            final String message,
            final RagContext ragContext,
            final int recentMessageLimit
    ) throws Exception {
        final List<ChatMessageDto> hybridContext = buildPersonSynthesisHybridContext(
                sessionId,
                message,
                recentMessageLimit
        );
        final PersonFocus personFocus = ragContext == null ? null : ragContext.personFocus();
        final List<RagSearchResult> results = ragContext == null || ragContext.results() == null
                ? List.of()
                : ragContext.results();
        final RagIntent intent = ragContext == null ? null : ragContext.intent();
        final PersonSynthesisResult result = personSynthesisHybridService.resolveHybrid(
                sessionId,
                message,
                intent,
                personFocus,
                results,
                toAiChatMessages(hybridContext),
                responseGuardService
        );
        return toResolvedChatResponse(result);
    }

    /**
     * {@link PersonSynthesisResult}를 채널 내부 {@link ResolvedChatResponse}로 매핑합니다.
     */
    private ResolvedChatResponse toResolvedChatResponse(final PersonSynthesisResult result) {
        if (result == null) {
            return new ResolvedChatResponse("", "RULE_PRIMARY", "empty_response");
        }
        return new ResolvedChatResponse(
                result.content(),
                result.responseMode(),
                result.guardDetail(),
                result.retryGuardDetail()
        );
    }

    /**
     * person SYNTHESIS hybrid에 넣을 최근 대화 맥락을 구성합니다.
     *
     * <p>전체 세션 히스토리 대신 최근 몇 턴만 포함해 follow-up 질문을 보조합니다.</p>
     */
    private List<ChatMessageDto> buildPersonSynthesisHybridContext(
            final Integer sessionId,
            final String currentMessage,
            final int recentMessageLimit
    ) throws Exception {
        final int cappedLimit = Math.min(Math.max(recentMessageLimit, 2), 12);
        final List<ChatMessageDto> recent = sanitizeContextMessages(
                chatMessageService.getRecentContextMessages(sessionId, cappedLimit)
        );
        if (recent.isEmpty()) {
            return List.of(ChatMessageDto.builder().role("USER").content(currentMessage).build());
        }

        final int hybridTurnLimit = 5;
        final int start = Math.max(0, recent.size() - hybridTurnLimit);
        return new ArrayList<>(recent.subList(start, recent.size()));
    }



    /**
     * 채널 {@link ChatMessageDto} 목록을 LLM 입력용 {@link AiChatMessage}로 변환한다.
     *
     * <p>{@code feature.ai}는 chat DTO에 의존하지 않으므로, 오케스트레이터에서 role/content만 넘긴다.</p>
     *
     * @param messages 세션 맥락 메시지 (null 허용)
     * @return AI 클라이언트에 전달할 메시지 목록
     */
    private List<AiChatMessage> toAiChatMessages(final List<ChatMessageDto> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }
        final List<AiChatMessage> out = new ArrayList<>(messages.size());
        for (final ChatMessageDto message : messages) {
            if (message == null) {
                continue;
            }
            out.add(AiChatMessage.builder()
                    .role(message.getRole())
                    .content(message.getContent())
                    .build());
        }
        return out;
    }

    /**
     * LLM 1차 응답에 언어·person hollow guard를 적용해 최종 본문과 responseMode를 만듭니다.
     */
    private ResolvedChatResponse resolveLlmChatResponse(
            final Integer sessionId,
            final String message,
            final RagContext ragContext,
            final String systemPrompt,
            final List<ChatMessageDto> contextMessages,
            final String initialStrippedResponse
    ) throws Exception {
        if (containsDisallowedHanScript(initialStrippedResponse)) {
            return new ResolvedChatResponse(
                    buildLanguageFallback(message, ragContext),
                    "LANGUAGE_FALLBACK",
                    "language_guard"
            );
        }
        if (!isDegradedPersonResponse(initialStrippedResponse, ragContext, message)) {
            return new ResolvedChatResponse(initialStrippedResponse, "LLM");
        }
        final String firstGuardDetail = describePersonGuardFailure(initialStrippedResponse, ragContext, message);
        log.warn("AI person response degraded, retrying once. sessionId={}, guardDetail={}",
                sessionId, firstGuardDetail);
        final String retryResponse = stripInternalRecordCitations(ollamaClient.chat(
                systemPrompt + buildPersonMeaningRetryPrompt(ragContext, message, firstGuardDetail),
                toAiChatMessages(contextMessages)
        ));
        if (!containsDisallowedHanScript(retryResponse)
                && !isDegradedPersonResponse(retryResponse, ragContext, message)) {
            return new ResolvedChatResponse(retryResponse, "LLM");
        }
        final String retryGuardDetail = describePersonGuardFailure(retryResponse, ragContext, message);
        log.warn("AI person retry still hollow, deterministic fallback applied. sessionId={}, guardDetail={}, retryGuardDetail={}",
                sessionId, firstGuardDetail, retryGuardDetail);
        // 태도(person-attitude) 질문은 RagContextService.detectIntent → SYNTHESIS 라우팅으로 Path C
        // (shouldUseRulePrimaryPersonSynthesisResponse)에서만 처리·폴백된다. 이 레거시 저하 경로는
        // LOOKUP 인물-의미/등장 질문만 도달하므로 PERSON_STANCE_FALLBACK 분기는 두지 않는다.
        if (isPersonAppearanceQuery(message)) {
            return new ResolvedChatResponse(
                    buildPersonAppearanceDeterministicFallback(ragContext),
                    "PERSON_APPEARANCE_FALLBACK",
                    firstGuardDetail,
                    retryGuardDetail
            );
        }
        return new ResolvedChatResponse(
                buildPersonMeaningDeterministicFallback(ragContext),
                "PERSON_MEANING_FALLBACK",
                firstGuardDetail,
                retryGuardDetail
        );
    }



    /**
     * 이전 assistant 응답이 언어 규칙을 어긴 경우 다음 프롬프트 맥락에서 제외합니다.
     *
     * <p>이미 저장된 잘못된 응답이 다음 생성에 다시 들어가면 모델이 같은 언어 패턴을 따라갈 수 있습니다.</p>
     *
     * @param messages 최근 대화 메시지
     * @return 언어 규칙 위반 assistant 메시지를 대체한 맥락 메시지
     */
    private List<ChatMessageDto> sanitizeContextMessages(final List<ChatMessageDto> messages) {
        if (messages == null) return List.of();
        return messages.stream()
                .map(message -> {
                    if (!isAssistantRole(message.getRole()) || !containsDisallowedHanScript(message.getContent())) {
                        return message;
                    }
                    return message.toBuilder()
                            .content("[이전 AI 응답은 언어 규칙 위반으로 맥락에서 제외되었습니다.]")
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * assistant 역할 여부를 확인합니다.
     */
    private boolean isAssistantRole(final String role) {
        return StringUtils.equalsAnyIgnoreCase(role, "ASSISTANT", "AI", "SYSTEM");
    }

    /**
     * 한국어 응답에 섞이면 안 되는 한자/중국어 계열 문자가 포함되었는지 확인합니다.
     *
     * <p>한국어 일반 응답에서 한자 1자는 우연히 포함될 수 있으므로 2자 이상부터 차단합니다.</p>
     */
    private boolean containsDisallowedHanScript(final String text) {
        return responseGuardService.containsDisallowedHanScript(text);
    }

    /**
     * person 가드 실패 시 UI/로그용 짧은 사유 코드를 반환합니다.
     */
    private String describePersonGuardFailure(
            final String response,
            final RagContext ragContext,
            final String queryText
    ) {
        return responseGuardService.describePersonGuardFailure(
                response,
                ragContext == null ? null : ragContext.personFocus(),
                ragContext == null || ragContext.results() == null ? List.of() : ragContext.results(),
                ragContext == null ? null : ragContext.intent(),
                queryText
        );
    }

    /**
     * SYNTHESIS person-meaning hollow guard와 LOOKUP 인물 태도 질문의 빈 주제 분류를 함께 검사합니다.
     */
    private boolean isDegradedPersonResponse(
            final String response,
            final RagContext ragContext,
            final String queryText
    ) {
        return responseGuardService.isDegradedPersonResponse(
                response,
                ragContext == null ? null : ragContext.personFocus(),
                ragContext == null || ragContext.results() == null ? List.of() : ragContext.results(),
                ragContext == null ? null : ragContext.intent(),
                queryText
        );
    }

    /**
     * Builds a one-shot retry prompt when person-meaning hollow guard rejects the first LLM answer.
     */
    private String buildPersonMeaningRetryPrompt(
            final RagContext ragContext,
            final String queryText,
            final String guardDetail
    ) {
        final PersonFocus personFocus = ragContext == null ? null : ragContext.personFocus();
        final List<RagSearchResult> results = ragContext == null || ragContext.results() == null
                ? List.of()
                : ragContext.results();
        return personSynthesisHybridService.buildPersonMeaningRetryPrompt(
                personFocus, results, queryText, guardDetail
        );
    }












    /**
     * 모델이 빈 분류만 내놓았을 때 스캐폴드 데이터로 결정적 person-meaning 답변을 만듭니다.
     */
    private String buildPersonMeaningDeterministicFallback(final RagContext ragContext) {
        if (ragContext == null) {
            return personSynthesisHybridService.buildPersonMeaningDeterministicFallback(null, List.of());
        }
        return personSynthesisHybridService.buildPersonMeaningDeterministicFallback(
                ragContext.personFocus(),
                ragContext.results() == null ? List.of() : ragContext.results()
        );
    }

    /**
     * 대화/기록 속 인물 등장 방식 질문용 규칙 기반 응답을 만듭니다.
     */
    private String buildPersonAppearanceDeterministicFallback(final RagContext ragContext) {
        if (ragContext == null) {
            return personSynthesisHybridService.buildPersonAppearanceDeterministicFallback(null, List.of());
        }
        return personSynthesisHybridService.buildPersonAppearanceDeterministicFallback(
                ragContext.personFocus(),
                ragContext.results() == null ? List.of() : ragContext.results()
        );
    }


    /**
     * LLM이 재시도 후에도 언어 규칙을 어긴 경우 저장할 안전한 한국어 응답을 만듭니다.
     */
    private String buildLanguageFallback(final String userMessage, final RagContext ragContext) {
        log.warn("AI response language guard fallback. query={}", StringUtils.abbreviate(userMessage, 80));
        if (ragContext == null || StringUtils.isBlank(ragContext.text())) {
            return chatMsg("chat.ai.language-fallback.no-context");
        }
        if (ragContext.intent() == RagIntent.SYNTHESIS) {
            return chatMsg("chat.ai.language-fallback.synthesis-retry");
        }
        return chatMsg("chat.ai.language-fallback.lookup-retry");
    }

    /**
     * AI 응답에서 RAG 내부 기록 인덱스([1], [2] 등) 인용을 제거합니다. 마크다운 기호는 보존하며 클라이언트에서 HTML로 렌더합니다.
     */
    private String stripInternalRecordCitations(final String text) {
        if (text == null) return null;
        return text
                .replaceAll("\\[(\\d{1,2})\\]\\s*기록", "기록")
                .replaceAll("\\[(\\d{1,2})\\]", "")
                .replaceAll(" {2,}", " ")
                .replaceAll(" (?=[.,!?])", "")
                .trim();
    }

    /**
     * LLM 경로에서 최종 본문과 responseMode를 함께 반환합니다.
     */
    private record ResolvedChatResponse(
            String content,
            String responseMode,
            String guardDetail,
            String retryGuardDetail
    ) {
        ResolvedChatResponse(final String content, final String responseMode) {
            this(content, responseMode, null, null);
        }

        ResolvedChatResponse(final String content, final String responseMode, final String guardDetail) {
            this(content, responseMode, guardDetail, null);
        }
    }

}
