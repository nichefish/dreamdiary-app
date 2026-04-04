package io.nicheblog.dreamdiary.feature.clsf.history.entity.embed;

import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;

/**
 * HistoryEmbedModule
 * <pre>
 *   History 모듈 인터페이스
 * </pre>
 *
 * @author nichefish
 * @see MapstructHelper
 */
public interface HistoryEmbedModule {
    /** Getter */
    HistoryEmbed getHistory();

    /** Setter */
    void setHistory(HistoryEmbed embed);
}
