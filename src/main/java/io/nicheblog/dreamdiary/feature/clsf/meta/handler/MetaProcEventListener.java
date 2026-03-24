package io.nicheblog.dreamdiary.feature.clsf.meta.handler;

import io.nicheblog.dreamdiary.feature.clsf.meta.event.MetaProcEvent;
import io.nicheblog.dreamdiary.global.handler.CustomEventBus;
import io.nicheblog.dreamdiary.global.handler.CustomEventHandler;
import io.nicheblog.dreamdiary.infrastructure.cache.handler.EhCacheEvictEventListner;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * MetaEventListener
 * <pre>
 *  메타 관련 이벤트 처리 핸들러.
 * </pre>
 *
 * @author nichefish
 * @see CustomEventBus
 */
@Component("metaProcEventListener")
@RequiredArgsConstructor
@Log4j2
public class MetaProcEventListener implements CustomEventHandler<MetaProcEvent> {

    private final MetaProcWorker metaProcWorker;

    /**
     * 메타 이벤트를 처리한다.
     *
     * @param event 처리할 이벤트 객체
     * @see EhCacheEvictEventListner
     */
    @Override
    public void handle(final MetaProcEvent event) throws Exception {
        log.debug("MetaProcEventListener.handle() - event : {}", event.toString());
        metaProcWorker.handle(event);
    }

    /**
     * 본 핸들러가 처리할 이벤트 타입.
     */
    @Override
    public Class<MetaProcEvent> getEventType() {
        return MetaProcEvent.class;
    }
}
