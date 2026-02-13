package io.nicheblog.dreamdiary.extension.clsf.state.model.cmpstn;

import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;

/**
 * StateCmpstnModule
 * <pre>
 *   상태 모듈 인터페이스
 * </pre>
 *
 * @author nichefish
 * @see MapstructHelper
 */
public interface StateCmpstnModule {
    /** Getter */
    StateCmpstn getState();

    /** Setter */
    void setState(StateCmpstn cmpstn);

    /** Set State */
    default void setStateFrom(StateCmpstnModule cmpstnModule) {
        setState(cmpstnModule.getState());
    }
}

