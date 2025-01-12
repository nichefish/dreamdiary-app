package io.nicheblog.dreamdiary.global._common.log.actvty.handler;

import io.nicheblog.dreamdiary.global._common.log.actvty.event.LogActvtyEvent;
import io.nicheblog.dreamdiary.global._common.log.actvty.event.LogAnonActvtyEvent;
import io.nicheblog.dreamdiary.global._common.log.sys.event.LogSysEvent;
import io.nicheblog.dreamdiary.global.config.AsyncConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * LogEventListener
 * <pre>
 *  로그 이벤트 처리 핸들러
 *  내부에 LogWorkerRunnable 구현 (Queue 처리)
 * </pre>
 *
 * @author nichefish
 * @see AsyncConfig
 */
@Component
@RequiredArgsConstructor
public class LogActvtyEventListener {

    private final LogActvtyWorker logActvtyWorker;

    /**
     * 현재 인증(로그인) 상태인 활동 로그 등록 이벤트를 처리한다.
     *
     * @param event 처리할 이벤트 객체
     */
    @EventListener
    @Async
    public void handleLogActvtyEvent(LogActvtyEvent event) {
        // 큐에 전달하기 전에 request 관련 속성들을 미리 바인딩해야 한다. (권장)
        logActvtyWorker.offer(event);
    }

    /**
     * 현재 미인증(비로그인) 상태인 활동 로그 등록 이벤트를 처리한다.
     *
     * @param event 처리할 이벤트 객체
     */
    @EventListener
    @Async
    public void handleLogAnonActvtyEvent(LogAnonActvtyEvent event) {
        // 큐에 전달하기 전에 request 관련 속성들을 미리 바인딩해야 한다. (권장)
        logActvtyWorker.offer(event);
    }

    /**
     * 시스템 로그 등록 이벤트를 처리한다.
     *
     * @param event 처리할 이벤트 객체
     */
    @EventListener
    @Async
    public void handleLogSysEvent(LogSysEvent event) {
        // 큐에 전달하기 전에 request 관련 속성들을 미리 바인딩해야 한다. (권장)
        logActvtyWorker.offer(event);
    }
}
