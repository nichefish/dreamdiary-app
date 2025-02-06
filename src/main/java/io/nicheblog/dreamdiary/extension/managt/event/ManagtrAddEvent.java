package io.nicheblog.dreamdiary.extension.managt.event;

import io.nicheblog.dreamdiary.extension.managt.handler.ManagtrEventListener;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * ManagtrAddEvent
 * <pre>
 *  조치자Managtr 추가 이벤트 :: 메인 로직과 분리
 * </pre>
 *
 * @author nichefish
 * @see ManagtrEventListener
 */
@Getter
public class ManagtrAddEvent
        extends ApplicationEvent {

    /** 컨텐츠 복합키 */
    private final BaseClsfKey clsfKey;

    /* ----- */

    /**
     * 생성자.
     *
     * @param source 이벤트의 출처를 나타내는 객체
     * @param clsfKey 이벤트 처리 대상 객체를 식별 가능한 복합키
     */
    public ManagtrAddEvent(final Object source, final BaseClsfKey clsfKey) {
        super(source);
        this.clsfKey = clsfKey;
    }
}
