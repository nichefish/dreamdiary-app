package io.nicheblog.dreamdiary.feature.clsf.history.service.strategy;

import io.nicheblog.dreamdiary.feature.clsf._shared.model.BaseClsfDto;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.history.HistoryType;
import io.nicheblog.dreamdiary.feature.clsf.history.model.HistoryDto;

import java.util.List;

/**
 * HistoryStrategy
 * <pre>
 *  contentType 별 history 조회/복구 전략.
 * </pre>
 *
 * @author nichefish
 */
public interface HistoryStrategy<Dto extends BaseClsfDto> {

    ContentType getContentType();

    Dto getOwnedDto(final String userId, final Integer key) throws Exception;

    Dto updtCn(
            final Integer key,
            final String cn,
            final HistoryType historyType,
            final Integer fromHistoryId
    ) throws Exception;

    default Dto applyHistoryList(final Dto currentDto, final List<HistoryDto> historyList) {
        return currentDto;
    }
}
