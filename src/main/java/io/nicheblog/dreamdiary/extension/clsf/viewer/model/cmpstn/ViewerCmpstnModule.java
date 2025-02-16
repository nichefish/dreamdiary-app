package io.nicheblog.dreamdiary.extension.clsf.viewer.model.cmpstn;

import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;

/**
 * ViewerCmpstnModule
 * <pre>
 *   Viewer 모듈 인터페이스
 * </pre>
 *
 * @author nichefish
 * @see MapstructHelper
 */
public interface ViewerCmpstnModule {
    /** Getter */
    ViewerCmpstn getViewer();

    /** Setter */
    void setViewer(ViewerCmpstn cmpstn);

    /** 새 글 여부 세팅 */
    void setIsNew(Boolean isNew);
}

