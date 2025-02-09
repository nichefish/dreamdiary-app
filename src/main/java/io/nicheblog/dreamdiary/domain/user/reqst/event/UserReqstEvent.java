package io.nicheblog.dreamdiary.domain.user.reqst.event;

import io.nicheblog.dreamdiary.domain.user.reqst.handler.UserReqstEventListener;
import io.nicheblog.dreamdiary.domain.user.reqst.model.UserReqstDto;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * UserReqstEvent
 * <pre>
 *  .사용자 등록 이벤트 :: 메인 로직과 분리.
 * </pre>
 *
 * @author nichefish
 * @see UserReqstEventListener
 */
@Getter
public class UserReqstEvent
        extends ApplicationEvent {

    /** 사용자 정보 */
    private final UserReqstDto userReqst;

    /* ----- */

    /**
     * 생성자.
     *
     * @param source 이벤트의 출처를 나타내는 객체
     * @param userReqst 사용자 신청 정보
     */
    public UserReqstEvent(final Object source, final UserReqstDto userReqst) {
        super(source);
        this.userReqst = userReqst;
    }
}
