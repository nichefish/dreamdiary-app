package io.nicheblog.dreamdiary.feature.chat.controller;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.chat.model.ChatMessageDto;
import io.nicheblog.dreamdiary.feature.chat.model.ChatMessageSearchParam;
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
 *  채팅 관련 컨트롤러.
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
     */
    @GetMapping("/chat/settings")
    @ResponseBody
    public ResponseEntity<AjaxResponse> getChatSettings() {
        final ChatSettingDto setting = chatSettingService.getMySetting();

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withObj(setting));
    }

    /**
     * 내 채팅 설정을 수정한다.
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
     */
    @GetMapping("/admin/chat/settings")
    @ResponseBody
    public ResponseEntity<AjaxResponse> getAdminChatSettings() {
        final ChatSettingDto setting = chatSettingService.getAdminSetting();

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withObj(setting));
    }

    /**
     * 관리자 채팅 기본 설정을 수정한다.
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
     */
    @GetMapping("/chat/sessions")
    @ResponseBody
    public ResponseEntity<AjaxResponse> getChatSessions() {
        final List<ChatSessionDto> sessionList = chatSessionService.getMySessions();

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withList(sessionList));
    }

    /**
     * 새 채팅 세션을 생성한다.
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
     * 기존 채팅 메시지를 DB에서 가져온다.
     *
     * @param searchParam 검색 파라미터
     * @return AjaxResponse를 포함한 ResponseEntity 객체
     */
    @GetMapping("/chat/messages")
    @ResponseBody
    public ResponseEntity<AjaxResponse> getChatMessages(
            final ChatMessageSearchParam searchParam
    ) throws Exception {

        final List<ChatMessageDto> messageList = chatMessageService.getListDto(searchParam);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(messageList));
    }

    /**
     * 세션의 채팅 메시지를 DB에서 가져온다.
     *
     * @param sessionId 채팅 세션 ID
     * @return AjaxResponse를 포함한 ResponseEntity 객체
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
     * 클라이언트로부터 메시지를 받아서 처리하고, 결과를 반환합니다.
     *
     * @param message 클라이언트로부터 받은 메시지
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
}
