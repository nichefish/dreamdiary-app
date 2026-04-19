package io.nicheblog.dreamdiary.feature.journal.dream.service.strategy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.history.HistoryType;
import io.nicheblog.dreamdiary.feature.attachable.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.attachable.history.service.strategy.HistoryStrategy;
import io.nicheblog.dreamdiary.feature.journal.dream.model.JournalDreamDto;
import io.nicheblog.dreamdiary.feature.journal.dream.service.JournalDreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JournalDreamHistoryStrategy
 * <pre>
 *  journal dream history 복구 전략.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class JournalDreamHistoryStrategy implements HistoryStrategy<JournalDreamDto> {

    private final JournalDreamService journalDreamService;

    @Override
    public ContentType getContentType() {
        return ContentType.JOURNAL_DREAM;
    }

    @Override
    public JournalDreamDto getOwnedDto(final String username, final Integer key) throws Exception {
        return journalDreamService.getDtlDtoWithCacheByUser(username, key);
    }

    @Override
    public JournalDreamDto updtContent(
            final Integer key,
            final String content,
            final HistoryType historyType,
            final Integer fromHistoryId
    ) throws Exception {
        return journalDreamService.updtContent(key, content, historyType, fromHistoryId);
    }

    @Override
    public JournalDreamDto applyHistoryList(final JournalDreamDto currentDto, final List<HistoryDto> historyList) {
        currentDto.setHistoryList(historyList);
        return currentDto;
    }
}
