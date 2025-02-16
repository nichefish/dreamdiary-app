package io.nicheblog.dreamdiary.extension.clsf.sectn.model.cmpstn;

import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;

/**
 * SectnCmpstnModule
 * <pre>
 *   단락 모듈 인터페이스
 * </pre>
 *
 * @author nichefish
 * @see MapstructHelper
 */
public interface SectnCmpstnModule {

    /** Getter */
    SectnCmpstn getSectn();

    /** Setter */
    void setSectn(SectnCmpstn cmpstn);
}

