package io.nicheblog.dreamdiary.global.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * CustomEventHandlerRegistry
 * 커스텀 이벤트 핸들러 맵 생성
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class CustomEventHandlerRegistry implements SmartInitializingSingleton {

    private final ApplicationContext ctx;
    private final Map<Class<?>, CustomEventHandler<?>> handlerMap = new HashMap<>();

    /**
     * 스프링 초기화 시점에 모든 CustomEventHandler 구현체를 스캔하여 핸들러 맵을 구성한다.
     * - ApplicationContext.getBeansOfType(CustomEventHandler) 로 모든 핸들러를 수집.
     * - 각 핸들러의 getEventType() 을 key 로 등록.
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void afterSingletonsInstantiated() {
        Map<String, CustomEventHandler> beans = ctx.getBeansOfType(CustomEventHandler.class);
        beans.values().forEach(handler -> handlerMap.put(handler.getEventType(), handler));
    }

    /**
     * 주어진 이벤트 객체의 클래스(event.getClass())에 대응하는 커스텀 핸들러를 조회한다.
     *
     * @param event 조회할 이벤트 객체
     * @return 대응되는 CustomEventHandler, 없으면 null
     */
    @SuppressWarnings("rawtypes")
    public CustomEventHandler getHandler(ApplicationEvent event) {
        return handlerMap.get(event.getClass());
    }
}
