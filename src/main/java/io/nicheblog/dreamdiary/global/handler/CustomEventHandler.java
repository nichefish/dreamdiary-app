package io.nicheblog.dreamdiary.global.handler;

import org.springframework.context.ApplicationEvent;

/**
 * CustomEventHandler
 * {@link CustomEventBus} 전용 이벤트 핸들러 인터페이스.
 *
 * @param <T>
 *
 * @author nichefish
 */
public interface CustomEventHandler<T extends ApplicationEvent> {

    /**
     * 실제 이벤트 로직을 처리한다.
     *
     * @param event CustomEventBus 로부터 전달된 이벤트 객체
     * @throws Exception 내부 처리 중 예외 발생 시 상위로 전달
     */
    void handle(final T event) throws Exception;

    /**
     * 본 핸들러가 처리할 이벤트 타입을 명시적으로 반환한다.
     * CustomEventHandlerRegistry 가 매핑을 구성할 때 사용된다.
     *
     * @return 본 핸들러가 처리하는 이벤트의 Class 타입
     */
    Class<T> getEventType();
}