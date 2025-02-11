package io.nicheblog.dreamdiary.global.handler;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * ApplicationEventPublisherWrapper
 * <pre>
 *  동기/비동기적으로 애플리케이션 이벤트를 발행합니다.
 * </pre>
 *
 * @author nichefish
 */
@Component
public class ApplicationEventPublisherWrapper {

    @Resource
    private ApplicationEventPublisher delegate;
    @Resource(name = "taskExecutor")
    private Executor asyncExecutor;

    /**
     * 동기적으로 애플리케이션 이벤트를 발행합니다.
     *
     * @param event 발행할 {@link ApplicationEvent}
     */
    public void publishEvent(@NotNull ApplicationEvent event) {
        delegate.publishEvent(event);
    }

    /**
     * 동기적으로 애플리케이션 이벤트를 발행합니다.
     *
     * @param event 발행할 {@link ApplicationEvent}
     */
    public void publishEvent(@NotNull Object event) {
        delegate.publishEvent(event);
    }

    /**
     * 애플리케이션 이벤트를 비동기적으로 발행합니다.
     * 비동기 실행 전, 현재의 {@link SecurityContext}를 보존하고,실행 후 정리합니다.
     *
     * @param event 발행할 {@link ApplicationEvent}
     */
    public void publishAsyncEvent(final @NotNull ApplicationEvent event) {
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
     * {@link ApplicationEvent} 이외 객체 타입의 이벤트를 비동기적으로 발행합니다.
     * 비동기 실행 전, 현재의 {@link SecurityContext}를 보존하고,실행 후 정리합니다.
     *
     * @param event 발행할 이벤트 객체
     */
    public void publishAsyncEvent(final @NotNull Object event) {
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

    /**
     * 이벤트를 비동기적으로 발행하고 완료될 때까지 기다립니다.
     *
     * @param event 비동기적으로 발행할 {@link ApplicationEvent}
     * @return 이벤트 처리 완료 여부를 나타내는 {@link CompletableFuture}
     */
    public CompletableFuture<Void> publishAsyncEventAndWait(ApplicationEvent event) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        CompletableFuture<Void> future = new CompletableFuture<>();

        asyncExecutor.execute(() -> {
            SecurityContextHolder.setContext(securityContext);
            try {
                delegate.publishEvent(event);
                future.complete(null); // 이벤트 처리 완료
            } catch (Exception e) {
                future.completeExceptionally(e); // 예외 발생 시 future 완료 처리
            } finally {
                SecurityContextHolder.clearContext();
            }
        });

        return future;
    }
}
