package io.nicheblog.dreamdiary.feature.attachable.history.service.strategy;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.history.HistoryType;
import io.nicheblog.dreamdiary.feature.attachable.history.model.HistoryDto;

import java.util.List;
import java.util.Set;

/**
 * HistoryStrategy
 * <pre>
 *  contentType 별 history 조회/복구 전략.
 * </pre>
 *
 * @author nichefish
 */
public interface HistoryStrategy<Dto extends BaseAttachableDto> {

    default ContentType getContentType() {
        return ContentType.DEFAULT;
    }

    default Set<ContentType> getContentTypes() {
        return Set.of(this.getContentType());
    }

    Dto getOwnedDto(final String username, final Integer key) throws Exception;

    default Dto getOwnedDto(final String username, final Integer key, final ContentType contentType) throws Exception {
        return this.getOwnedDto(username, key);
    }

    Dto updtContent(
            final Integer key,
            final String content,
            final HistoryType historyType,
            final Integer fromHistoryId
    ) throws Exception;

    default Dto updtContent(
            final Integer key,
            final String content,
            final HistoryType historyType,
            final Integer fromHistoryId,
            final ContentType contentType
    ) throws Exception {
        return this.updtContent(key, content, historyType, fromHistoryId);
    }

    default Dto applyHistoryList(final Dto currentDto, final List<HistoryDto> historyList) {
        return currentDto;
    }
}
