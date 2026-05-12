package io.nicheblog.dreamdiary.feature.attachable.viewer.event;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable.viewer.handler.ViewerEventListener;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * ViewerAddEvent
 * <pre>
 *  컨텐츠 열람자 추가 이벤트. :: 메인 로직과 분리
 * </pre>
 *
 * @author nichefish
 * @see ViewerEventListener
 */
@Getter
public class ViewerAddEvent
        extends ApplicationEvent {

    /** 보안 컨텍스트 */
    private final SecurityContext securityContext;
    /** 컨텐츠 복합키 */
    private final BaseAttachableKey attachableKey;

    /* ----- */

    /**
     * 생성자.
     *
     * @param source 이 이벤트의 출처를 나타내는 객체
     * @param attachableKey 이벤트 처리 대상 객체를 식별 가능한 복합키
     */
    public ViewerAddEvent(final Object source, final BaseAttachableKey attachableKey) {
        super(source);
        this.securityContext = SecurityContextHolder.getContext();
        this.attachableKey = attachableKey;
    }
}
