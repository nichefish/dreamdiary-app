package io.nicheblog.dreamdiary.feature.clsf.tag.handler;

import io.nicheblog.dreamdiary.feature.clsf.tag.event.TagProcEvent;
import io.nicheblog.dreamdiary.global.handler.CustomEventBus;
import io.nicheblog.dreamdiary.global.handler.CustomEventHandler;
import io.nicheblog.dreamdiary.infrastructure.cache.handler.EhCacheEvictEventListner;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * TagProcEventListener
 * <pre>
 *  태그 관련 이벤트 처리 핸들러.
 * </pre>
 *
 * @author nichefish
 * @see CustomEventBus
 */
@Component("tagProcEventListener")
@RequiredArgsConstructor
@Log4j2
public class TagProcEventListener implements CustomEventHandler<TagProcEvent> {

    private final TagProcWorker tagProcWorker;

    /**
     * 태그 이벤트를 처리한다.
     *
     * @param event 처리할 이벤트 객체
     * @see EhCacheEvictEventListner
     */
    @Override
    public void handle(final TagProcEvent event) throws Exception {
        log.debug("TagProcEventListener.handle() - event : {}", event.toString());
        tagProcWorker.handle(event);
    }

    /**
     * 본 핸들러가 처리할 이벤트 타입.
     */
    @Override
    public Class<TagProcEvent> getEventType() {
        return TagProcEvent.class;
    }
}
