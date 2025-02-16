package io.nicheblog.dreamdiary.extension.clsf.tag.model.cmpstn;

import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;

/**
 * TagCmpstnModule
 * <pre>
 *   Tag 모듈 인터페이스
 * </pre>
 *
 * @author nichefish
 * @see MapstructHelper
 */
public interface TagCmpstnModule {
    /** Getter */
    TagCmpstn getTag();

    /** Setter */
    void setTag(TagCmpstn cmpstn);
}

