package io.nicheblog.dreamdiary.extension.clsf.managt.handler;

import io.nicheblog.dreamdiary.extension.clsf.managt.event.ManagtrAddEvent;
import io.nicheblog.dreamdiary.extension.clsf.managt.service.ManagtrService;
import io.nicheblog.dreamdiary.global.config.AsyncConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * ManagtrEventListener
 * <pre>
 *  컨탠츠 조치자 이벤트 처리 핸들러.
 * </pre>
 *
 * @author nichefish
 * @see AsyncConfig
 */
@Component
@RequiredArgsConstructor
public class ManagtrEventListener {

    private final ManagtrService managtrService;

    /**
     * 조치자 추가.
     *
     * @param event 처리할 이벤트 객체
     */
    @EventListener
    @Async
    public void handleManagtrAddEvent(final ManagtrAddEvent event) {
        // 조치자 추가
        managtrService.addManagtr(event.getClsfKey());
    }
}
