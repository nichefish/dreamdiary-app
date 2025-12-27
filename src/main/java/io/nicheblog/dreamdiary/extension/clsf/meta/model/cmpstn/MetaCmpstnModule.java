package io.nicheblog.dreamdiary.extension.clsf.meta.model.cmpstn;

import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;

/**
 * MetaCmpstnModule
 * <pre>
 *   Meta 모듈 인터페이스
 * </pre>
 *
 * @author nichefish
 * @see MapstructHelper
 */
public interface MetaCmpstnModule {
    /** Getter */
    MetaCmpstn getMeta();

    /** Setter */
    void setMeta(MetaCmpstn cmpstn);

    /** Set Meta */
    default void setMetaFrom(MetaCmpstnModule cmpstnModule) {
        setMeta(cmpstnModule.getMeta());
    }
}

