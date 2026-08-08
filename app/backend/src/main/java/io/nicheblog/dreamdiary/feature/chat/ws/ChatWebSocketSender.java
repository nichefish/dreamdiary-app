package io.nicheblog.dreamdiary.feature.chat.ws;

import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 채팅 세션 STOMP 토픽으로 PROGRESS/DELTA·메시지 AjaxResponse를 브로드캐스트한다.
 *
 * <p>토픽: {@code /topic/chat/session/{sessionId}}. 진행·델타 이벤트는 클라이언트가
 * {@code messages}에 넣지 않고 UI 메타/임시 버블에만 쓴다.</p>
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class ChatWebSocketSender {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 채팅 세션 구독자에게 AI 응답 진행 단계(SEARCHING/GENERATING)를 브로드캐스트한다.
     * 메시지 본문이 아니라 진행 UI용 메타 이벤트이며, 클라이언트는 messages에 넣지 않는다.
     *
     * @param sessionId 채팅 세션 ID
     * @param phase {@code SEARCHING} 또는 {@code GENERATING}
     */
    public void broadcastProgress(final Integer sessionId, final String phase) {
        if (sessionId == null || StringUtils.isBlank(phase)) {
            return;
        }
        final Map<String, Object> progress = new LinkedHashMap<>();
        progress.put("type", "PROGRESS");
        progress.put("sessionId", sessionId);
        progress.put("phase", phase);
        send(sessionId, AjaxResponse.withAjaxResult(true, null).withObj(progress));
        log.info("AI response progress. sessionId={}, phase={}", sessionId, phase);
    }

    /**
     * 채팅 세션 구독자에게 assistant 응답의 부분 텍스트(DELTA)를 브로드캐스트한다.
     * 메시지 목록에 넣지 않고, 클라이언트가 임시 버블에 누적한다.
     * 완성 ASSISTANT 메시지가 도착하면 임시 버블을 교체한다.
     *
     * @param sessionId 채팅 세션 ID
     * @param delta Ollama 스트림 청크 텍스트
     */
    public void broadcastDelta(final Integer sessionId, final String delta) {
        if (sessionId == null || StringUtils.isEmpty(delta)) {
            return;
        }
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "DELTA");
        payload.put("sessionId", sessionId);
        payload.put("delta", delta);
        send(sessionId, AjaxResponse.withAjaxResult(true, null).withObj(payload));
    }

    /**
     * 세션 토픽으로 등록 완료 메시지(AjaxResponse)를 브로드캐스트한다.
     *
     * @param sessionId 채팅 세션 ID
     * @param response USER/ASSISTANT 등록 결과 응답
     */
    public void broadcastMessage(final Integer sessionId, final AjaxResponse response) {
        if (sessionId == null || response == null) {
            return;
        }
        send(sessionId, response);
    }

    private void send(final Integer sessionId, final AjaxResponse response) {
        messagingTemplate.convertAndSend("/topic/chat/session/" + sessionId, response);
    }
}
