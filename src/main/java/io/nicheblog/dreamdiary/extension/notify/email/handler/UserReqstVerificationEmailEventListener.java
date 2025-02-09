package io.nicheblog.dreamdiary.extension.notify.email.handler;

import io.nicheblog.dreamdiary.domain.user.reqst.handler.UserReqstWorker;
import io.nicheblog.dreamdiary.domain.user.reqst.model.UserReqstDto;
import io.nicheblog.dreamdiary.extension.notify.email.event.EmailSendEvent;
import io.nicheblog.dreamdiary.extension.notify.email.event.UserReqstVerificationEmailSendEvent;
import io.nicheblog.dreamdiary.extension.notify.email.model.EmailAddress;
import io.nicheblog.dreamdiary.extension.notify.email.model.EmailSendParam;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.ServerInfo;
import io.nicheblog.dreamdiary.global.config.AsyncConfig;
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
 * UserReqstVerificationEmailEventListener
 * <pre>
 *  사용자 등록 인증번호 발송 이벤트 처리 핸들러.
 * </pre>
 *
 * @author nichefish
 * @see AsyncConfig
 * @see UserReqstWorker
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class UserReqstVerificationEmailEventListener {

    private final EmailSendWorker emailSendWorker;
    private final ServerInfo serverInfo;

    /**
     * 사용자 등록 이메일 발송 이벤트를 처리한다.
     *
     * @param event 처리할 이벤트 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserReqstEvent(final UserReqstVerificationEmailSendEvent event) throws Exception {

        try {
            final UserReqstDto userReqst = event.getUserReqst();
            final String securityCode = event.getSecurityCode();
            final String domain = serverInfo.getDomain() + ":" + serverInfo.getPort();
            final String verifyUrl = "http://" + domain + "/auth/verify.do/" + securityCode;

            final EmailSendParam emailSendParam = EmailSendParam.builder()
                    .recipientList(Collections.singletonList(new EmailAddress(userReqst.getEmail(), userReqst.getNickNm())))
                    .sender(Constant.SYSTEM_EMAIL_ADDRESS)
                    .subject("Dreamdiary 계정 신청 인증번호")
                    .tmplat("email/user_reqst_verification_code.ftlh")
                    .dataMap(Map.of("securityCode", securityCode, "recipientName", userReqst.getNickNm(), "authenticationUrl", verifyUrl))
                    .build();
            final EmailSendEvent emailSendEvent = new EmailSendEvent(event.getSource(), emailSendParam);
            emailSendWorker.offer(emailSendEvent);
        } catch (Exception e) {
            log.error("Error handling UserReqstVerificationEmailSendEvent: {}", e.getMessage(), e);
        }
    }
}
