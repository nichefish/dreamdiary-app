package io.nicheblog.dreamdiary.feature.chat.service;

import io.nicheblog.dreamdiary.feature.chat.client.OllamaClient;
import io.nicheblog.dreamdiary.feature.chat.entity.ChatSessionEntity;
import io.nicheblog.dreamdiary.feature.chat.model.ChatMessageDto;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

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

    private final ChatMessageService chatMessageService;
    private final ChatSessionService chatSessionService;
    private final ChatSettingService chatSettingService;
    private final OllamaClient ollamaClient;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 사용자 메시지를 저장한 뒤 최근 대화 맥락을 포함해 AI 응답을 생성하고 세션 구독자에게 전송한다.
     *
     * @param sessionId 메시지가 속한 채팅 세션 ID
     * @param message 사용자 입력 메시지
     * @throws Exception 세션 검증, 메시지 저장, AI 호출 중 예외가 발생한 경우
     */
    public void processChat(final Integer sessionId, final String message) throws Exception {
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

        // 3. AI 응답 생성
        final int recentMessageLimit = chatSettingService.getMyRecentMessageLimit();
        final String aiResponse = ollamaClient.chat(
                        StringUtils.defaultIfBlank(session.getSystemPrompt(), chatSessionService.getDefaultSystemPrompt()),
                        chatMessageService.getRecentContextMessages(sessionId, recentMessageLimit)
                );

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
    }
}
