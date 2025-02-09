package io.nicheblog.dreamdiary.domain.user.reqst.handler;

import io.nicheblog.dreamdiary.domain.user.reqst.event.UserReqstEvent;
import io.nicheblog.dreamdiary.global.config.AsyncConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserReqstEventListener
 * <pre>
 *  사용자 등록 이벤트 처리 핸들러.
 * </pre>
 *
 * @author nichefish
 * @see AsyncConfig
 */
@Component
@RequiredArgsConstructor
public class UserReqstEventListener {

    private final UserReqstWorker userReqstWorker;

    /**
     * 사용자 등록 이벤트를 처리한다.
     * 삭제된 엔티티 재조회와 관련될 수 있으므로 별도 트랜잭션으로 처리.
     * 
     * @param event 처리할 이벤트 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserReqstEvent(final UserReqstEvent event) throws Exception {
        // 큐에 전달하기 전에 request 관련 속성들을 미리 바인딩해야 한다. (권장)
        userReqstWorker.offer(event);
    }
}
