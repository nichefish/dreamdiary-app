package io.nicheblog.dreamdiary.extension.clsf.tag.handler;

import io.nicheblog.dreamdiary.extension.cache.handler.EhCacheEvictEventListner;
import io.nicheblog.dreamdiary.extension.clsf.tag.event.JrnlTagProcEvent;
import io.nicheblog.dreamdiary.global.handler.CustomEventBus;
import io.nicheblog.dreamdiary.global.handler.CustomEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * JrnlTagProcEventListener
 * <pre>
 *  저널 태그 관련 이벤트 처리 핸들러.
 * </pre>
 *
 * @author nichefish
 * @see CustomEventBus
 */
@Component("jrnlTagProcEventListener")
@RequiredArgsConstructor
@Log4j2
public class JrnlTagProcEventListener implements CustomEventHandler<JrnlTagProcEvent> {

    private final TagProcWorker tagProcWorker;

    /**
     * 태그 이벤트를 처리한다.
     *
     * @param event 처리할 이벤트 객체
     * @see EhCacheEvictEventListner
     */
    @Override
    public void handle(final JrnlTagProcEvent event) throws Exception {
        log.debug("TagProcEventListener.handle() - event : {}", event.toString());

        tagProcWorker.handle(event);
    }

    /**
     * 본 핸들러가 처리할 이벤트 타입을 명시적으로 반환한다.
     * CustomEventHandlerRegistry 가 매핑을 구성할 때 사용된다.
     *
     * @return 본 핸들러가 처리하는 이벤트의 Class 타입
     */
    @Override
    public Class<JrnlTagProcEvent> getEventType() {
        return JrnlTagProcEvent.class;
    }
}
