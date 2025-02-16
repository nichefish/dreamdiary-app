package io.nicheblog.dreamdiary.extension.clsf.state.entity.embed;

import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;

/**
 * StateEmbedModule
 * <pre>
 *   State 모듈 인터페이스 (entity level)
 * </pre>
 *
 * @author nichefish
 * @see MapstructHelper
 */
public interface StateEmbedModule {
    /** Getter */
    StateEmbed getState();

    /** Setter */
    void setState(StateEmbed embed);
}
