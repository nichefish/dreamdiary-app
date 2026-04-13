package io.nicheblog.dreamdiary.feature.jrnl.intrpt.service.strategy;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.history.HistoryType;
import io.nicheblog.dreamdiary.feature.clsf.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.clsf.history.service.strategy.HistoryStrategy;
import io.nicheblog.dreamdiary.feature.jrnl.intrpt.model.JrnlIntrptDto;
import io.nicheblog.dreamdiary.feature.jrnl.intrpt.service.JrnlIntrptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JrnlIntrptHistoryStrategy
 * <pre>
 *  journal intrpt history 복구 전략.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class JrnlIntrptHistoryStrategy implements HistoryStrategy<JrnlIntrptDto> {

    private final JrnlIntrptService jrnlIntrptService;

    @Override
    public ContentType getContentType() {
        return ContentType.JRNL_INTRPT;
    }

    @Override
    public JrnlIntrptDto getOwnedDto(final String userId, final Integer key) throws Exception {
        return jrnlIntrptService.getDtlDtoWithCacheByUser(userId, key);
    }

    @Override
    public JrnlIntrptDto updtCn(
            final Integer key,
            final String cn,
            final HistoryType historyType,
            final Integer fromHistoryId
    ) throws Exception {
        return jrnlIntrptService.updtCn(key, cn, historyType, fromHistoryId);
    }

    @Override
    public JrnlIntrptDto applyHistoryList(final JrnlIntrptDto currentDto, final List<HistoryDto> historyList) {
        currentDto.setHistoryList(historyList);
        return currentDto;
    }
}
