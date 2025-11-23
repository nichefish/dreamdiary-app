package io.nicheblog.dreamdiary.extension.cache.handler;

import io.nicheblog.dreamdiary.extension.cache.event.JrnlCacheEvictEvent;
import io.nicheblog.dreamdiary.global.handler.CustomEventBus;
import io.nicheblog.dreamdiary.global.handler.CustomEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * JrnlCacheEvictEventListner
 * <pre>
 *  저널 캐시 제거 이벤트 처리 핸들러.
 *  데이터 정합성을 위해 동기 처리
 * </pre>
 *
 * @author nichefish
 * @see CustomEventBus
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JrnlCacheEvictEventListner implements CustomEventHandler<JrnlCacheEvictEvent> {

    private final JrnlCacheEvictWorker jrnlCacheEvictWorker;

    /**
     * 저널 캐시 제거 이벤트를 처리한다.
     *
     * @param event 처리할 이벤트 객체
     */
    @Override
    public void handle(final JrnlCacheEvictEvent event) throws Exception {
        log.debug("JrnlCacheEvictEventListner.handleJrnlCacheEvictEvent() - event : {}", event.toString());

        jrnlCacheEvictWorker.handle(event);
    }

    /**
     * 본 핸들러가 처리할 이벤트 타입을 명시적으로 반환한다.
     * CustomEventHandlerRegistry 가 매핑을 구성할 때 사용된다.
     *
     * @return 본 핸들러가 처리하는 이벤트의 Class 타입
     */
    @Override
    public Class<JrnlCacheEvictEvent> getEventType() {
        return JrnlCacheEvictEvent.class;
    }
}
