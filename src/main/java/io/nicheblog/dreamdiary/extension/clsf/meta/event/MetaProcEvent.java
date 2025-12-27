package io.nicheblog.dreamdiary.extension.clsf.meta.event;

import io.nicheblog.dreamdiary.extension.clsf.meta.handler.MetaProcEventListener;
import io.nicheblog.dreamdiary.extension.clsf.meta.model.cmpstn.MetaCmpstn;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * MetaProcEvent
 * <pre>
 *  태그 처리(추가/삭제) 이벤트 :: 메인 로직과 분리.
 * </pre>
 *
 * @author nichefish
 * @see MetaProcEventListener
 */
@Getter
public class MetaProcEvent
        extends ApplicationEvent {

    /** 보안 컨텍스트 */
    private final SecurityContext securityContext;
    /** 컨텐츠 복합키 */
    private final BaseClsfKey clsfKey;
    /** 태그 */
    private final MetaCmpstn metaCmpstn;

    /* ----- */

    /**
     * 생성자.
     *
     * @param source 이벤트의 출처를 나타내는 객체
     * @param clsfKey 해당 이벤트에 대한 분류 키
     * @param meta 태그 조합 객체 (MetaCmpstn)
     */
    public MetaProcEvent(final Object source, final BaseClsfKey clsfKey, final MetaCmpstn meta) {
        super(source);
        this.securityContext = SecurityContextHolder.getContext();
        this.clsfKey = clsfKey;
        this.metaCmpstn = meta;
    }

    /**
     * 생성자.
     * @param source 이벤트의 출처를 나타내는 객체
     * @param clsfKey 해당 이벤트에 대한 분류 키
     */
    public MetaProcEvent(final Object source, final BaseClsfKey clsfKey) {
        super(source);
        this.securityContext = SecurityContextHolder.getContext();
        this.clsfKey = clsfKey;
        this.metaCmpstn = null;
    }
}
