package io.nicheblog.dreamdiary.feature.user.signup.event;

import io.nicheblog.dreamdiary.feature.user.signup.handler.UserSignupEventListener;
import io.nicheblog.dreamdiary.feature.user.signup.model.UserSignupRequestDto;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * UserSignupEvent
 * <pre>
 *  사용자 계정 신청 처리용 애플리케이션 이벤트 (메인 로직과 분리).
 * </pre>
 *
 * 명명 규약: 라이프사이클·비동기 흐름 이벤트는 {@code UserSignup*}; 페이로드가 신청 레코드 DTO 이면 {@link UserSignupRequestDto} 로 명시한다.
 *
 * @author nichefish
 * @see UserSignupEventListener
 */
@Getter
public class UserSignupEvent
        extends ApplicationEvent {

    /** 사용자 신청(요청) 페이로드 */
    private final UserSignupRequestDto userSignupRequest;

    /* ----- */

    /**
     * 생성자.
     *
     * @param source 이벤트의 출처를 나타내는 객체
     * @param userSignupRequest 사용자 신청 정보
     */
    public UserSignupEvent(final Object source, final UserSignupRequestDto userSignupRequest) {
        super(source);
        this.userSignupRequest = userSignupRequest;
    }
}
