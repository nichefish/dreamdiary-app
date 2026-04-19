package io.nicheblog.dreamdiary.feature.attachable.history.model.cmpstn;

import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;

/**
 * HistoryCmpstnModule
 * <pre>
 *   History 모듈 인터페이스
 * </pre>
 *
 * @author nichefish
 * @see MapstructHelper
 */
public interface HistoryCmpstnModule {
    /** Getter */
    HistoryCmpstn getHistory();

    /** Setter */
    void setHistory(HistoryCmpstn cmpstn);
}

