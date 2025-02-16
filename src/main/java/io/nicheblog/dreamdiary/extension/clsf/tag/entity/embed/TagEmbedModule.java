package io.nicheblog.dreamdiary.extension.clsf.tag.entity.embed;

import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;

/**
 * TagEmbedModule
 * <pre>
 *   Tag 모듈 인터페이스
 * </pre>
 *
 * @author nichefish
 * @see MapstructHelper
 */
public interface TagEmbedModule {
    /** Getter */
    TagEmbed getTag();

    /** Setter */
    void setTag(TagEmbed embed);
}
