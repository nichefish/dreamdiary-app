package io.nicheblog.dreamdiary.feature.journal.diary.service.strategy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.history.HistoryType;
import io.nicheblog.dreamdiary.feature.attachable.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.attachable.history.service.strategy.HistoryStrategy;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryDto;
import io.nicheblog.dreamdiary.feature.journal.diary.service.JournalDiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JournalDiaryHistoryStrategy
 * <pre>
 *  journal diary history 복구 전략.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class JournalDiaryHistoryStrategy implements HistoryStrategy<JournalDiaryDto> {

    private final JournalDiaryService journalDiaryService;

    @Override
    public ContentType getContentType() {
        return ContentType.JOURNAL_DIARY;
    }

    @Override
    public JournalDiaryDto getOwnedDto(final String username, final Integer key) throws Exception {
        return journalDiaryService.getDtlDtoWithCacheByUser(username, key);
    }

    @Override
    public JournalDiaryDto updtContent(
            final Integer key,
            final String content,
            final HistoryType historyType,
            final Integer fromHistoryId
    ) throws Exception {
        return journalDiaryService.updtContent(key, content, historyType, fromHistoryId);
    }

    @Override
    public JournalDiaryDto applyHistoryList(final JournalDiaryDto currentDto, final List<HistoryDto> historyList) {
        currentDto.setHistoryList(historyList);
        return currentDto;
    }
}
