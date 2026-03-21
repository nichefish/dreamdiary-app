package io.nicheblog.dreamdiary.feature.user.reqst.event;

import io.nicheblog.dreamdiary.feature.user.reqst.handler.UserReqstVerificationEmailEventListener;
import io.nicheblog.dreamdiary.feature.user.reqst.model.UserReqstDto;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * UserReqstVerificationEmailEvent
 * <pre>
 *  사용자 등록 인증코드 발송 (Email) 이벤트 :: 메인 로직과 분리.
 * </pre>
 *
 * @author nichefish
 * @see UserReqstVerificationEmailEventListener
 */
@Getter
public class UserReqstVerificationEmailSendEvent
        extends ApplicationEvent {

    /** 사용자 정보 */
    private final UserReqstDto userReqst;
    /** 인증코드 */
    private final String securityCode;

    /* ----- */

    /**
     * 생성자.
     *
     * @param source 이벤트의 출처를 나타내는 객체
     * @param userReqst 사용자 요청 객체
     * @param securityCode 인증코드
     */
    public UserReqstVerificationEmailSendEvent(final Object source, final UserReqstDto userReqst, final String securityCode) {
        super(source);
        this.userReqst = userReqst;
        this.securityCode = securityCode;
    }
}
