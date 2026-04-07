package io.nicheblog.dreamdiary.feature.jrnl.dream.service.strategy;

import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.history.HistoryType;
import io.nicheblog.dreamdiary.feature.clsf.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.clsf.history.service.strategy.HistoryStrategy;
import io.nicheblog.dreamdiary.feature.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.feature.jrnl.dream.service.JrnlDreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JrnlDreamHistoryStrategy
 * <pre>
 *  journal dream history 복구 전략.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class JrnlDreamHistoryStrategy implements HistoryStrategy<JrnlDreamDto> {

    private final JrnlDreamService jrnlDreamService;

    @Override
    public ContentType getContentType() {
        return ContentType.JRNL_DREAM;
    }

    @Override
    public JrnlDreamDto getOwnedDto(final String userId, final Integer key) throws Exception {
        return jrnlDreamService.getDtlDtoWithCacheByUser(userId, key);
    }

    @Override
    public JrnlDreamDto updtCn(
            final Integer key,
            final String cn,
            final HistoryType historyType,
            final Integer fromHistoryNo
    ) throws Exception {
        return jrnlDreamService.updtCn(key, cn, historyType, fromHistoryNo);
    }

    @Override
    public JrnlDreamDto applyHistoryList(final JrnlDreamDto currentDto, final List<HistoryDto> historyList) {
        currentDto.setHistoryList(historyList);
        return currentDto;
    }
}
