package io.nicheblog.dreamdiary.feature.journal.note.service.strategy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.history.HistoryType;
import io.nicheblog.dreamdiary.feature.attachable.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.attachable.history.service.strategy.HistoryStrategy;
import io.nicheblog.dreamdiary.feature.journal.note.model.JournalNoteDto;
import io.nicheblog.dreamdiary.feature.journal.note.service.JournalNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JournalNoteHistoryStrategy
 * <pre>
 *  journal note history 복구 전략.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class JournalNoteHistoryStrategy implements HistoryStrategy<JournalNoteDto> {

    private final JournalNoteService journalNoteService;

    @Override
    public ContentType getContentType() {
        return ContentType.JOURNAL_NOTE;
    }

    @Override
    public JournalNoteDto getOwnedDto(final String username, final Integer key) throws Exception {
        return journalNoteService.getDtlDtoWithCacheByUser(username, key);
    }

    @Override
    public JournalNoteDto updtContent(
            final Integer key,
            final String content,
            final HistoryType historyType,
            final Integer fromHistoryId
    ) throws Exception {
        return journalNoteService.updtContent(key, content, historyType, fromHistoryId);
    }

    @Override
    public JournalNoteDto applyHistoryList(final JournalNoteDto currentDto, final List<HistoryDto> historyList) {
        currentDto.setHistoryList(historyList);
        return currentDto;
    }
}
