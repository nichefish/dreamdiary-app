package io.nicheblog.dreamdiary.feature.chat.controller;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.chat.model.ChatMessageDto;
import io.nicheblog.dreamdiary.feature.chat.model.ChatSettingDto;
import io.nicheblog.dreamdiary.feature.chat.model.ChatSessionDto;
import io.nicheblog.dreamdiary.feature.chat.service.ChatAIService;
import io.nicheblog.dreamdiary.feature.chat.service.ChatMessageService;
import io.nicheblog.dreamdiary.feature.chat.service.ChatSettingService;
import io.nicheblog.dreamdiary.feature.chat.service.ChatSessionService;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * ChatController
 * <pre>
 *  채팅 설정, 세션, 메시지 조회와 WebSocket 메시지 전송을 담당하는 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final ChatSessionService chatSessionService;
    private final ChatSettingService chatSettingService;
    private final ChatAIService chatAIService;

    /**
     * 내 채팅 설정을 조회한다.
     *
     * @return 로그인 사용자의 채팅 설정을 담은 Ajax 응답
     */
    @GetMapping("/chat/settings")
    @ResponseBody
    public ResponseEntity<AjaxResponse> getChatSettings() {
        final ChatSettingDto setting = chatSettingService.getMySetting();

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withObj(setting));
    }

    /**
     * 내 채팅 설정을 수정한다.
     *
     * @param settingDto 변경할 사용자 채팅 설정
     * @return 저장된 사용자 채팅 설정을 담은 Ajax 응답
     */
    @PatchMapping("/chat/settings")
    @ResponseBody
    public ResponseEntity<AjaxResponse> modifyChatSettings(
            final @RequestBody ChatSettingDto settingDto
    ) {
        final ChatSettingDto setting = chatSettingService.modifyMySetting(settingDto);

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withObj(setting));
    }

    /**
     * 관리자 채팅 기본 설정을 조회한다.
     *
     * @return 전역 기본 채팅 설정을 담은 Ajax 응답
     */
    @GetMapping("/admin/chat/settings")
    @ResponseBody
    public ResponseEntity<AjaxResponse> getAdminChatSettings() {
        final ChatSettingDto setting = chatSettingService.getAdminSetting();

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withObj(setting));
    }

    /**
     * 관리자 채팅 기본 설정을 수정한다.
     *
     * @param settingDto 변경할 전역 기본 채팅 설정
     * @return 저장된 전역 기본 채팅 설정을 담은 Ajax 응답
     */
    @PatchMapping("/admin/chat/settings")
    @ResponseBody
    public ResponseEntity<AjaxResponse> modifyAdminChatSettings(
            final @RequestBody ChatSettingDto settingDto
    ) {
        final ChatSettingDto setting = chatSettingService.modifyAdminSetting(settingDto);

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withObj(setting));
    }

    /**
     * 내 채팅 세션 목록을 조회한다.
     *
     * @return 로그인 사용자의 채팅 세션 목록을 담은 Ajax 응답
     */
    @GetMapping("/chat/sessions")
    @ResponseBody
    public ResponseEntity<AjaxResponse> getChatSessions() {
        final List<ChatSessionDto> sessionList = chatSessionService.getMySessions();

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withList(sessionList));
    }

    /**
     * 새 채팅 세션을 생성한다.
     *
     * @param sessionDto 생성 시 적용할 세션 제목, 모델, 시스템 프롬프트
     * @return 생성된 채팅 세션을 담은 Ajax 응답
     */
    @PostMapping("/chat/sessions")
    @ResponseBody
    public ResponseEntity<AjaxResponse> createChatSession(
            final @RequestBody(required = false) ChatSessionDto sessionDto
    ) {
        final ChatSessionDto createdSession = chatSessionService.create(sessionDto);

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withObj(createdSession));
    }

    /**
     * 채팅 세션을 삭제한다.
     *
     * @param sessionId 삭제할 채팅 세션 ID
     * @return 삭제 처리 결과 Ajax 응답
     */
    @DeleteMapping("/chat/sessions/{sessionId}")
    @ResponseBody
    public ResponseEntity<AjaxResponse> deleteChatSession(
            final @PathVariable("sessionId") Integer sessionId
    ) {
        chatSessionService.delete(sessionId);

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS));
    }

    /**
     * 세션의 채팅 메시지를 DB에서 가져온다.
     * 변경 전: {@code GET /chat/messages}가 검색 파라미터만으로 기존 채팅 메시지를 DB에서 가져왔다.
     * 변경 후: 세션 소유권을 먼저 검증하는 이 경로만 메시지 이력을 제공한다.
     *
     * @param sessionId 채팅 세션 ID
     * @return AjaxResponse를 포함한 ResponseEntity 객체
     * @throws Exception 메시지 조회 중 예외가 발생한 경우
     */
    @GetMapping("/chat/sessions/{sessionId}/messages")
    @ResponseBody
    public ResponseEntity<AjaxResponse> getChatSessionMessages(
            final @PathVariable("sessionId") Integer sessionId
    ) throws Exception {

        chatSessionService.getMySessionEntity(sessionId);
        final List<ChatMessageDto> messageList = chatMessageService.getSessionMessages(sessionId);

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withList(messageList));
    }

    /**
     * 클라이언트로부터 WebSocket 메시지를 받아 사용자/AI 메시지 저장과 브로드캐스트를 처리한다.
     *
     * @param sessionId 메시지를 보낼 채팅 세션 ID
     * @param message 클라이언트로부터 받은 메시지
     * @param stompHeaderAccessor WebSocket 세션 인증 정보를 담은 STOMP 헤더 접근자
     * @throws Exception 메시지 저장 또는 AI 응답 생성 중 예외가 발생한 경우
     */
    @MessageMapping("/chat/session/{sessionId}/send")
    public void sendMessage(
            final @DestinationVariable("sessionId") Integer sessionId,
            final @Payload String message,
            final StompHeaderAccessor stompHeaderAccessor
    ) throws Exception {

        log.info("ChatController.sendMessage() sessionId: {}, message: {}", sessionId, message);

        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException(MessageUtils.getExceptionMsg("IllegalArgumentException.empty-msg"));
        }

        // WebSocket 세션에서 attributes 가져오기
        final Authentication authentication = (Authentication) Objects.requireNonNull(stompHeaderAccessor.getSessionAttributes()).get("authentication");
        AuthUtils.setAuthentication(authentication);

        chatAIService.processChat(sessionId, message);
    }

    /**
     * 클라이언트로부터 응답 취소 요청을 받아 해당 세션의 AI 응답 생성을 중단한다.
     *
     * @param sessionId 취소할 채팅 세션 ID
     * @param stompHeaderAccessor WebSocket 세션 인증 정보를 담은 STOMP 헤더 접근자
     */
    @MessageMapping("/chat/session/{sessionId}/cancel")
    public void cancelMessage(
            final @DestinationVariable("sessionId") Integer sessionId,
            final StompHeaderAccessor stompHeaderAccessor
    ) {
        final Authentication authentication = (Authentication) Objects.requireNonNull(stompHeaderAccessor.getSessionAttributes()).get("authentication");
        AuthUtils.setAuthentication(authentication);

        chatAIService.cancelChat(sessionId);
    }
}
