package io.nicheblog.dreamdiary.feature.journal.interpretation.service.strategy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.history.HistoryType;
import io.nicheblog.dreamdiary.feature.attachable.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.attachable.history.service.strategy.HistoryStrategy;
import io.nicheblog.dreamdiary.feature.journal.interpretation.model.JournalInterpretationDto;
import io.nicheblog.dreamdiary.feature.journal.interpretation.service.JournalInterpretationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JournalInterpretationHistoryStrategy
 * <pre>
 *  journal interpretation history 복구 전략.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class JournalInterpretationHistoryStrategy implements HistoryStrategy<JournalInterpretationDto> {

    private final JournalInterpretationService journalInterpretationService;

    @Override
    public ContentType getContentType() {
        return ContentType.JOURNAL_INTERPRETATION;
    }

    @Override
    public JournalInterpretationDto getOwnedDto(final String username, final Integer key) throws Exception {
        return journalInterpretationService.getDtlDtoWithCacheByUser(username, key);
    }

    @Override
    public JournalInterpretationDto updtContent(
            final Integer key,
            final String content,
            final HistoryType historyType,
            final Integer fromHistoryId
    ) throws Exception {
        return journalInterpretationService.updtContent(key, content, historyType, fromHistoryId);
    }

    @Override
    public JournalInterpretationDto applyHistoryList(final JournalInterpretationDto currentDto, final List<HistoryDto> historyList) {
        currentDto.setHistoryList(historyList);
        return currentDto;
    }
}
