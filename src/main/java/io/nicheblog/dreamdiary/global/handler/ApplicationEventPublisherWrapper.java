package io.nicheblog.dreamdiary.global.handler;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * ApplicationEventPublisherWrapper
 * <pre>
 *  동기/비동기적으로 애플리케이션 이벤트를 발행한다.
 * </pre>
 *
 * @author nichefish
 */
@Component
@Log4j2
public class ApplicationEventPublisherWrapper {

    @Resource
    private ApplicationEventPublisher delegate;
    @Resource(name = "taskExecutor")
    private Executor asyncExecutor;
    @Resource(name = "customEventBus")
    private CustomEventBus customEventBus;

    /**
     * 동기적으로 애플리케이션 이벤트를 발행한다.
     *
     * @param event 발행할 {@link ApplicationEvent}
     */
    public void publishEvent(final @NotNull ApplicationEvent event) {
        log.debug("ApplicationEventPublisherWrapper.publishEvent() : {}", event);
        delegate.publishEvent(event);
    }

    /**
     * 동기적으로 애플리케이션 이벤트를 발행한다.
     *
     * @param event 발행할 {@link ApplicationEvent}
     */
    public void publishEvent(final @NotNull Object event) {
        log.debug("ApplicationEventPublisherWrapper.publishEvent() : {}", event);
        delegate.publishEvent(event);
    }

    /**
     * 동기적으로 커스텀 애플리케이션 이벤트를 발행한다.
     * {@link CustomEventBus} 활용, FIFO 보장이 필요할 경우에만 커스텀 이벤스버스 사용
     *
     * @param event 발행할 {@link ApplicationEvent}
     */
    public void publishCustomEvent(final @NotNull Object event) throws Exception {
        log.debug("ApplicationEventPublisherWrapper.publishCustomEvent() : {}", event);
        if (!(event instanceof ApplicationEvent)) return;
        Future<?> future = customEventBus.publishEvent((ApplicationEvent) event);
        future.get();
    }

    /**
     * 애플리케이션 이벤트를 비동기적으로 발행한다.
     *
     * @param event 발행할 {@link ApplicationEvent}
     */
    public void publishAsyncEvent(final @NotNull ApplicationEvent event) {
        log.debug("ApplicationEventPublisherWrapper.publishAsyncEvent() : {}", event);
        SecurityContext securityContext = SecurityContextHolder.getContext(); // 현재 SecurityContext 저장

        asyncExecutor.execute(() -> {
            SecurityContextHolder.setContext(securityContext); // 비동기 실행 전에 SecurityContext 설정
            try {
                delegate.publishEvent(event);
            } finally {
                SecurityContextHolder.clearContext(); // 실행 후 SecurityContext 정리
            }
        });
    }

    /**
     * {@link ApplicationEvent} 이외 객체 타입의 이벤트를 비동기적으로 발행한다.
     * 비동기 실행 전, 현재의 {@link SecurityContext}를 보존하고,실행 후 정리한다.
     *
     * @param event 발행할 이벤트 객체
     */
    public void publishAsyncEvent(final @NotNull Object event) {
        log.debug("ApplicationEventPublisherWrapper.publishAsyncEvent() : {}", event);
        SecurityContext securityContext = SecurityContextHolder.getContext();

        asyncExecutor.execute(() -> {
            SecurityContextHolder.setContext(securityContext);
            try {
                delegate.publishEvent(event);
            } finally {
                SecurityContextHolder.clearContext();
            }
        });
    }
}
