package io.nicheblog.dreamdiary.feature.attachable.history.service.strategy;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.history.HistoryType;
import io.nicheblog.dreamdiary.feature.attachable.history.model.HistoryDto;

import java.util.List;

/**
 * HistoryStrategy
 * <pre>
 *  contentType 별 history 조회/복구 전략.
 * </pre>
 *
 * @author nichefish
 */
public interface HistoryStrategy<Dto extends BaseAttachableDto> {

    ContentType getContentType();

    Dto getOwnedDto(final String username, final Integer key) throws Exception;

    Dto updtContent(
            final Integer key,
            final String content,
            final HistoryType historyType,
            final Integer fromHistoryId
    ) throws Exception;

    default Dto applyHistoryList(final Dto currentDto, final List<HistoryDto> historyList) {
        return currentDto;
    }
}
