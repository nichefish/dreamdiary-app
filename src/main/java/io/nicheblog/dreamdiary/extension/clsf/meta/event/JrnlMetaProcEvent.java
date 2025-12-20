package io.nicheblog.dreamdiary.extension.clsf.meta.event;

import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.extension.clsf.meta.handler.MetaProcEventListener;
import io.nicheblog.dreamdiary.extension.clsf.meta.model.cmpstn.MetaCmpstn;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
import lombok.Getter;

/**
 * JrnlMetaProcEvent
 * <pre>
 *  메타 추가 이벤트 :: 메인 로직과 분리.
 * </pre>
 *
 * @author nichefish
 * @see MetaProcEventListener
 */
@Getter
public class JrnlMetaProcEvent
        extends MetaProcEvent {

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
     * @param meta 메타 조합 객체 (MetaCmpstn)
     */
    public JrnlMetaProcEvent(final Object source, final BaseClsfKey clsfKey, final Integer yy, final Integer mnth, final MetaCmpstn meta) {
        super(source, clsfKey, meta);
        this.yy = yy;
        this.mnth = mnth;
    }

    /**
     * 생성자.
     * @param source 이벤트의 출처를 나타내는 객체
     * @param clsfKey 해당 이벤트에 대한 분류 키
     */
    public JrnlMetaProcEvent(final Object source, final BaseClsfKey clsfKey, final Integer yy, final Integer mnth) {
        super(source, clsfKey, null);
        this.yy = yy;
        this.mnth = mnth;
    }

    /**
     * 컨텐츠 타입 반환.
     *
     * @return {@link ContentType} -- 컨텐츠 타입
     */
    public ContentType getContentType() {
        if (this.getClsfKey() == null) return ContentType.DEFAULT;
        return this.getClsfKey().getContentTypeEnum();
    }
}
