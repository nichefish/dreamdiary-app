package io.nicheblog.dreamdiary.extension.viewer.model.cmpstn;

/**
 * ViewerCmpstnModule
 * <pre>
 *   Viewer 모듈 인터페이스
 * </pre>
 *
 * @author nichefish
 */
public interface ViewerCmpstnModule {
    /** Getter */
    ViewerCmpstn getViewer();

    /** Setter */
    void setViewer(ViewerCmpstn cmpstn);

    /** 새 글 여부 세팅 */
    void setIsNew(Boolean isNew);
}

