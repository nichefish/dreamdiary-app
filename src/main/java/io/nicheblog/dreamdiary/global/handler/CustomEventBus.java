package io.nicheblog.dreamdiary.global.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * CustomEventBus
 * 커스텀 이벤트 버스
 * Spring EventListener를 대체하는 자체 FIFO 기반 이벤트 디스패처.
 * 이벤트를 LinkedBlockingQueue에 넣고 단일 Worker Thread가 순서대로 처리함으로써 "강제적 순차 실행"을 제공한다.
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class CustomEventBus {

    private final CustomEventHandlerRegistry registry;

    /** FIFO Queue */
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    /** Worker Thread */
    private Thread worker;

    /** Worker 동작 여부 */
    private volatile boolean running = true;

    /**
     * Worker Thread 를 초기화하고 실행을 시작한다.
     */
    @PostConstruct
    public void start() {
        worker = new Thread(() -> {
            while (running) {
                try {
                    final Runnable task = queue.take();   // FIFO 가져오기
                    task.run();                     // 이벤트 실행
                } catch (Exception e) {
                    log.error("EventBus Worker error: {}", e.getMessage(), e);
                }
            }
        }, "dreamdiary-eventbus-worker");

        worker.start();
        log.info("[EventBus] Worker thread started.");
    }

    /**
     * Worker Thread 를 안전하게 종료한다.
     */
    @PreDestroy
    public void stop() {
        running = false;
        worker.interrupt();
    }

    /**
     * 비동기 이벤트 제출 (순서 보장)
     *
     * @param event 처리할 ApplicationEvent (핸들러가 반드시 있어야 한다.)
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void publishAsyncEvent(final ApplicationEvent event) throws Exception {
        final CustomEventHandler handler = registry.getHandler(event);
        if (handler == null) {
            throw new Exception("[EventBus] No handler found for " + event.getClass());
        }

        queue.offer(() -> {
            try {
                handler.handle(event);
            } catch (Exception e) {
                log.error("[EventBus] Error handling event: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * 동기 이벤트 제출 (Future로 완료까지 기다림)
     *
     * @param event 처리할 ApplicationEvent
     * @return 이벤트 처리 완료를 추적할 Future 객체
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Future<?> publishEvent(final ApplicationEvent event) throws Exception {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        final CustomEventHandler handler = registry.getHandler(event);
        if (handler == null) {
            throw new Exception("[EventBus] No handler found for " + event.getClass());
        }

        queue.offer(() -> {
            try {
                handler.handle(event);
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }
}
