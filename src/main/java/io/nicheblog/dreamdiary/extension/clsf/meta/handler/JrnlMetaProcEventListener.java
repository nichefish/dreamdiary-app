package io.nicheblog.dreamdiary.extension.clsf.meta.handler;

import io.nicheblog.dreamdiary.extension.cache.handler.EhCacheEvictEventListner;
import io.nicheblog.dreamdiary.extension.clsf.meta.event.JrnlMetaProcEvent;
import io.nicheblog.dreamdiary.global.handler.CustomEventBus;
import io.nicheblog.dreamdiary.global.handler.CustomEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * JrnlMetaProcEventListener
 * <pre>
 *  저널 메타 관련 이벤트 처리 핸들러.
 * </pre>
 *
 * @author nichefish
 * @see CustomEventBus
 */
@Component("jrnlMetaProcEventListener")
@RequiredArgsConstructor
@Log4j2
public class JrnlMetaProcEventListener implements CustomEventHandler<JrnlMetaProcEvent> {

    private final MetaProcWorker metaProcWorker;

    /**
     * 메타 이벤트를 처리한다.
     *
     * @param event 처리할 이벤트 객체
     * @see EhCacheEvictEventListner
     */
    @Override
    public void handle(final JrnlMetaProcEvent event) throws Exception {
        log.debug("MetaProcEventListener.handle() - event : {}", event.toString());

        metaProcWorker.handle(event);
    }

    /**
     * 본 핸들러가 처리할 이벤트 타입을 명시적으로 반환한다.
     * CustomEventHandlerRegistry 가 매핑을 구성할 때 사용된다.
     *
     * @return 본 핸들러가 처리하는 이벤트의 Class 타입
     */
    @Override
    public Class<JrnlMetaProcEvent> getEventType() {
        return JrnlMetaProcEvent.class;
    }
}
