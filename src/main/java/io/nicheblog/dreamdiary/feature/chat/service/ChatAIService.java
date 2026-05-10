package io.nicheblog.dreamdiary.feature.chat.service;

import io.nicheblog.dreamdiary.feature.chat.client.OllamaClient;
import io.nicheblog.dreamdiary.feature.chat.model.ChatMessageDto;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * ChatAIService
 * <pre>
 *  채팅 메세지 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class ChatAIService {

    private final ChatMessageService chatMessageService;
    private final OllamaClient ollamaClient;
    private final SimpMessagingTemplate messagingTemplate;

    public void processChat(final String message) throws Exception {
        // 1. 사용자 메시지 저장
        final ChatMessageDto userMessage = ChatMessageDto.builder()
                        .role("USER")
                        .content(message)
                        .build();
        final ServiceResponse userResult = chatMessageService.regist(userMessage);

        // 2. 사용자 메시지 broadcast
        messagingTemplate.convertAndSend(
                "/topic/chat",
                AjaxResponse.fromResponseWithObj(
                        userResult,
                        MessageUtils.RSLT_SUCCESS
                )
        );

        // 3. AI 응답 생성
        final String aiResponse = ollamaClient.chat(
                        """
                        너는 Dreamdiary assistant다. 사용자의 기록과 생각 정리를 돕는다.
                        """,
                        message
                );

        // 4. AI 메시지 저장
        final ChatMessageDto aiMessage = ChatMessageDto.builder()
                        .role("ASSISTANT")
                        .title("Dreamdiary AI")
                        .content(aiResponse)
                        .build();
        final ServiceResponse aiResult = chatMessageService.regist(aiMessage);

        // 5. AI 메시지 broadcast
        messagingTemplate.convertAndSend(
                "/topic/chat",
                AjaxResponse.fromResponseWithObj(
                        aiResult,
                        MessageUtils.RSLT_SUCCESS
                )
        );
    }
}
