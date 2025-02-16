package io.nicheblog.dreamdiary.extension.clsf.managt.model.cmpstn;

import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;

/**
 * ManagtCmpstnModule
 * <pre>
 *   Managt 모듈 인터페이스
 * </pre>
 *
 * @author nichefish
 * @see MapstructHelper
 */
public interface ManagtCmpstnModule {
    /** Getter */
    ManagtCmpstn getManagt();

    /** Setter */
    void setManagt(ManagtCmpstn cmpstn);
}

