package io.nicheblog.dreamdiary.global._common._clsf.tag.event;

import io.nicheblog.dreamdiary.global._common._clsf.tag.handler.TagEventListener;
import io.nicheblog.dreamdiary.global._common._clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * TagAddEvent
 * <pre>
 *  태그 추가 이벤트 :: 메인 로직과 분리.
 * </pre>
 *
 * @author nichefish
 * @see TagEventListener
 */
@Getter
public class TagProcEvent
        extends ApplicationEvent {

    /** 컨텐츠 복합키 */
    private final BaseClsfKey clsfKey;

    /** 태그 */
    private final TagCmpstn tagCmpstn;

    /* ----- */

    /**
     * 생성자.
     *
     * @param source 이벤트의 출처를 나타내는 객체
     * @param clsfKey 해당 이벤트에 대한 분류 키
     * @param tag 태그 조합 객체 (TagCmpstn)
     */
    public TagProcEvent(final Object source,  final BaseClsfKey clsfKey, final TagCmpstn tag) {
        super(source);
        this.clsfKey = clsfKey;
        this.tagCmpstn = tag;
    }

    /**
     * 생성자.
     * @param source 이벤트의 출처를 나타내는 객체
     * @param clsfKey 해당 이벤트에 대한 분류 키
     */
    public TagProcEvent(final Object source, final BaseClsfKey clsfKey) {
        super(source);
        this.clsfKey = clsfKey;
        this.tagCmpstn = null;
    }
}
