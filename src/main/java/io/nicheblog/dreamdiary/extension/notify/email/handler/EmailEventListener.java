package io.nicheblog.dreamdiary.extension.notify.email.handler;

import io.nicheblog.dreamdiary.extension.notify.email.event.EmailSendEvent;
import io.nicheblog.dreamdiary.global.config.AsyncConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * EmailEventListener
 * <pre>
 *  메일 이벤트 처리 핸들러
 * </pre>
 *
 * @author nichefish
 * @see AsyncConfig
 */
@Component
@RequiredArgsConstructor
public class EmailEventListener {

    private final EmailSendWorker mailSendWorker;

    /**
     * 메일 발송 이벤트를 처리한다.
     * 삭제된 엔티티 재조회와 관련될 수 있으므로 별도 트랜잭션으로 처리.
     *
     * @param event 처리할 이벤트 객체
     */
    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleMailEvent(final EmailSendEvent event) {
        // 큐에 전달하기 전에 request 관련 속성들을 미리 바인딩해야 한다. (권장)
        mailSendWorker.offer(event);
    }
}
