package io.nicheblog.dreamdiary.extension.clsf.managt.entity.embed;

import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;

/**
 * ManagtEmbedModule
 * <pre>
 *   Managt 모듈 인터페이스
 * </pre>
 *
 * @author nichefish
 * @see MapstructHelper
 */
public interface ManagtEmbedModule {
    /** Getter */
    ManagtEmbed getManagt();

    /** Setter */
    void setManagt(ManagtEmbed embed);
}
