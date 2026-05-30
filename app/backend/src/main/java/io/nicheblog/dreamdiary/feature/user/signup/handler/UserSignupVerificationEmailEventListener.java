package io.nicheblog.dreamdiary.feature.user.signup.handler;

import io.nicheblog.dreamdiary.feature.user.signup.event.UserSignupVerificationEmailSendEvent;
import io.nicheblog.dreamdiary.feature.user.signup.model.UserSignupRequestDto;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.ServerInfo;
import io.nicheblog.dreamdiary.global.config.AsyncConfig;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.infrastructure.messaging.email.event.EmailSendEvent;
import io.nicheblog.dreamdiary.infrastructure.messaging.email.model.EmailAddress;
import io.nicheblog.dreamdiary.infrastructure.messaging.email.model.EmailSendParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

/**
 * UserSignupVerificationEmailEventListener
 * <pre>
 *  사용자 등록 인증번호 발송 이벤트 처리 핸들러.
 * </pre>
 *
 * @author nichefish
 * @see AsyncConfig
 * @see UserSignupWorker
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class UserSignupVerificationEmailEventListener {

    private final ApplicationEventPublisherWrapper publisher;
    private final ServerInfo serverInfo;

    /**
     * 사용자 등록 이메일 발송 이벤트를 처리한다.
     *
     * @param event 처리할 이벤트 객체
     */
    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserSignupVerificationEmailEvent(final UserSignupVerificationEmailSendEvent event) {
        log.debug("UserSignupVerificationEmailEventListener.handleUserSignupVerificationEmailEvent() - event : {}", event.toString());

        try {
            final UserSignupRequestDto userSignupRequest = event.getUserSignupRequest();
            final String securityCode = event.getSecurityCode();
            final String domain = serverInfo.getDomain() + ":" + serverInfo.getPort();
            final String verifyUrl = "http://" + domain + "/auth/verify.do/" + securityCode;

            final EmailSendParam emailSendParam = EmailSendParam.builder()
                    .recipientList(Collections.singletonList(new EmailAddress(userSignupRequest.getEmail(), userSignupRequest.getNickname())))
                    .sender(new EmailAddress(Constant.SYSTEM_EMAIL, Constant.SYSTEM_ADMIN_NM))
                    .subject("Dreamdiary 계정 신청 인증번호")
                    .tmplat("email/user_signup_verification_code.ftlh")
                    .dataMap(Map.of("securityCode", securityCode, "recipientName", userSignupRequest.getNickname(), "authenticationUrl", verifyUrl))
                    .build();
            final EmailSendEvent emailSendEvent = new EmailSendEvent(event.getSource(), emailSendParam);
            publisher.publishAsyncEvent(emailSendEvent);
        } catch (Exception e) {
            log.error("Error handling UserSignupVerificationEmailSendEvent: {}", e.getMessage(), e);
        }
    }
}
