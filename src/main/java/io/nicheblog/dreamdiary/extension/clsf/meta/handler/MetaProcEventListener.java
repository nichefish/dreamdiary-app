package io.nicheblog.dreamdiary.extension.clsf.meta.handler;

import io.nicheblog.dreamdiary.extension.cache.handler.EhCacheEvictEventListner;
import io.nicheblog.dreamdiary.extension.clsf.meta.event.MetaProcEvent;
import io.nicheblog.dreamdiary.global.config.AsyncConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * MetaEventListener
 * <pre>
 *  메타 관련 이벤트 처리 핸들러.
 * </pre>
 *
 * @author nichefish
 * @see AsyncConfig
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class MetaProcEventListener {

    private final MetaProcWorker metaProcWorker;

    /**
     * 메타 이벤트를 처리한다.
     *
     * @param event 처리할 이벤트 객체
     * @see EhCacheEvictEventListner
     */
    @EventListener
    public void handleMetaProcEvent(final MetaProcEvent event) throws Exception {
        log.debug("MetaProcEventListener.handleMetaProcEvent() - event : {}", event.toString());

        metaProcWorker.handle(event);
    }
}
