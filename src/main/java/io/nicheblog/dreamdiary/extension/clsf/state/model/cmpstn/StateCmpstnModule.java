package io.nicheblog.dreamdiary.extension.clsf.state.model.cmpstn;

import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;

/**
 * StateCmpstnModule
 * <pre>
 *   State 모듈 인터페이스 (dto level)
 * </pre>
 *
 * @author nichefish
 * @see MapstructHelper
 */
public interface StateCmpstnModule {
    /** Getter */
    StateCmpstn getState();

    /** Setter */
    void setState(StateCmpstn embed);
}

