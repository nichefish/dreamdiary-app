package io.nicheblog.dreamdiary.extension.clsf.sectn.entity.embed;

import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;

/**
 * SectnEmbedModule
 * <pre>
 *   단락 모듈 인터페이스
 * </pre>
 *
 * @author nichefish
 * @see MapstructHelper
 */
public interface SectnEmbedModule {

    /** Getter */
    SectnEmbed getSectn();

    /** Setter */
    void setSectn(SectnEmbed embed);
}
