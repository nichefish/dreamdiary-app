package io.nicheblog.dreamdiary.feature.user.signup.event;

import io.nicheblog.dreamdiary.feature.user.signup.handler.UserSignupVerificationEmailEventListener;
import io.nicheblog.dreamdiary.feature.user.signup.model.UserSignupRequestDto;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * UserSignupVerificationEmailSendEvent
 * <pre>
 *  계정 신청 인증(검증) 메일 발송 이벤트 :: 메인 로직과 분리.
 * </pre>
 *
 * 명명 규약: 기능 흐름 이벤트는 {@code UserSignup*}; 수신인·내용 원천은 {@link UserSignupRequestDto}.
 *
 * @author nichefish
 * @see UserSignupVerificationEmailEventListener
 */
@Getter
public class UserSignupVerificationEmailSendEvent
        extends ApplicationEvent {

    /** 사용자 신청 정보 */
    private final UserSignupRequestDto userSignupRequest;
    /** 인증코드 */
    private final String securityCode;

    /* ----- */

    /**
     * 생성자.
     *
     * @param source 이벤트의 출처를 나타내는 객체
     * @param userSignupRequest 사용자 신청 객체
     * @param securityCode 인증코드
     */
    public UserSignupVerificationEmailSendEvent(final Object source, final UserSignupRequestDto userSignupRequest, final String securityCode) {
        super(source);
        this.userSignupRequest = userSignupRequest;
        this.securityCode = securityCode;
    }
}
