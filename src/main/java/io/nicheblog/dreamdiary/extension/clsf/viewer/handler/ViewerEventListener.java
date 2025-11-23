package io.nicheblog.dreamdiary.extension.clsf.viewer.handler;

import io.nicheblog.dreamdiary.extension.clsf.viewer.event.ViewerAddEvent;
import io.nicheblog.dreamdiary.extension.clsf.viewer.service.ViewerService;
import io.nicheblog.dreamdiary.global.config.AsyncConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * ViewerEventListener
 * <pre>
 *  컨탠츠 열람자 이벤트 처리 핸들러.
 * </pre>
 *
 * @author nichefish
 * @see AsyncConfig
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class ViewerEventListener {

    private final ViewerService viewerService;

    /**
     * 열람자 추가 이벤트를 처리합니다.
     *
     * @param event 처리할 이벤트 객체
     */
    @EventListener
    @Async
    public void handleViewerAddEvent(final ViewerAddEvent event) {
        log.debug("ViewerEventListener.handleViewerAddEvent() - event : {}", event.toString());

        // 열람자 추가
        viewerService.addViewer(event.getClsfKey());
    }
}
