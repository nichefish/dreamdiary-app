package io.nicheblog.dreamdiary.feature.jrnl.diary.service.strategy;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.history.HistoryType;
import io.nicheblog.dreamdiary.feature.clsf.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.clsf.history.service.strategy.HistoryStrategy;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.feature.jrnl.diary.service.JrnlDiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JrnlDiaryHistoryStrategy
 * <pre>
 *  journal diary history 복구 전략.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class JrnlDiaryHistoryStrategy implements HistoryStrategy<JrnlDiaryDto> {

    private final JrnlDiaryService jrnlDiaryService;

    @Override
    public ContentType getContentType() {
        return ContentType.JRNL_DIARY;
    }

    @Override
    public JrnlDiaryDto getOwnedDto(final String userId, final Integer key) throws Exception {
        return jrnlDiaryService.getDtlDtoWithCacheByUser(userId, key);
    }

    @Override
    public JrnlDiaryDto updtCn(
            final Integer key,
            final String cn,
            final HistoryType historyType,
            final Integer fromHistoryNo
    ) throws Exception {
        return jrnlDiaryService.updtCn(key, cn, historyType, fromHistoryNo);
    }

    @Override
    public JrnlDiaryDto applyHistoryList(final JrnlDiaryDto currentDto, final List<HistoryDto> historyList) {
        currentDto.setHistoryList(historyList);
        return currentDto;
    }
}
