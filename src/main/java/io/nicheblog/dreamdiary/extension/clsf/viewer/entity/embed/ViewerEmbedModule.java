package io.nicheblog.dreamdiary.extension.clsf.viewer.entity.embed;

import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;

/**
 * ViewerEmbedModule
 * <pre>
 *   Viewer 모듈 인터페이스
 * </pre>
 *
 * @author nichefish
 * @see MapstructHelper
 */
public interface ViewerEmbedModule {
    /** Getter */
    ViewerEmbed getViewer();

    /** Setter */
    void setViewer(ViewerEmbed embed);
}
