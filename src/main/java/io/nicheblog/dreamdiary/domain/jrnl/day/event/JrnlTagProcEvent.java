package io.nicheblog.dreamdiary.domain.jrnl.day.event;

import io.nicheblog.dreamdiary.extension.clsf.tag.event.TagProcEvent;
import io.nicheblog.dreamdiary.extension.clsf.tag.handler.TagProcEventListener;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
import lombok.Getter;

/**
 * JrnlTagProcEvent
 * <pre>
 *  태그 추가 이벤트 :: 메인 로직과 분리.
 * </pre>
 *
 * @author nichefish
 * @see TagProcEventListener
 */
@Getter
public class JrnlTagProcEvent
        extends TagProcEvent {

    /** 컨텐츠 복합키 */
    private final Integer yy;
    /** 컨텐츠 복합키 */
    private final Integer mnth;

    /* ----- */

    /**
     * 생성자.
     *
     * @param source 이벤트의 출처를 나타내는 객체
     * @param clsfKey 해당 이벤트에 대한 분류 키
     * @param tag 태그 조합 객체 (TagCmpstn)
     */
    public JrnlTagProcEvent(final Object source, final BaseClsfKey clsfKey, final Integer yy, final Integer mnth, final TagCmpstn tag) {
        super(source, clsfKey, tag);
        this.yy = yy;
        this.mnth = mnth;
    }

    /**
     * 생성자.
     * @param source 이벤트의 출처를 나타내는 객체
     * @param clsfKey 해당 이벤트에 대한 분류 키
     */
    public JrnlTagProcEvent(final Object source, final BaseClsfKey clsfKey, final Integer yy, final Integer mnth) {
        super(source, clsfKey, null);
        this.yy = yy;
        this.mnth = mnth;
    }
}
