package io.nicheblog.dreamdiary.feature.journal.intrpt.service.strategy;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.history.HistoryType;
import io.nicheblog.dreamdiary.feature.clsf.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.clsf.history.service.strategy.HistoryStrategy;
import io.nicheblog.dreamdiary.feature.journal.intrpt.model.JournalIntrptDto;
import io.nicheblog.dreamdiary.feature.journal.intrpt.service.JournalIntrptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JournalIntrptHistoryStrategy
 * <pre>
 *  journal intrpt history 복구 전략.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class JournalIntrptHistoryStrategy implements HistoryStrategy<JournalIntrptDto> {

    private final JournalIntrptService journalIntrptService;

    @Override
    public ContentType getContentType() {
        return ContentType.JOURNAL_INTRPT;
    }

    @Override
    public JournalIntrptDto getOwnedDto(final String username, final Integer key) throws Exception {
        return journalIntrptService.getDtlDtoWithCacheByUser(username, key);
    }

    @Override
    public JournalIntrptDto updtContent(
            final Integer key,
            final String content,
            final HistoryType historyType,
            final Integer fromHistoryId
    ) throws Exception {
        return journalIntrptService.updtContent(key, content, historyType, fromHistoryId);
    }

    @Override
    public JournalIntrptDto applyHistoryList(final JournalIntrptDto currentDto, final List<HistoryDto> historyList) {
        currentDto.setHistoryList(historyList);
        return currentDto;
    }
}
