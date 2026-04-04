package io.nicheblog.dreamdiary.feature.jrnl.diary.service;

import io.nicheblog.dreamdiary.feature.clsf.history.HistoryType;
import io.nicheblog.dreamdiary.feature.clsf.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.clsf.history.service.HistoryService;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryPostDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JrnlDiaryHistoryService {

    private final JrnlDiaryService jrnlDiaryService;
    private final HistoryService historyService;

    @Transactional(readOnly = true)
    public List<HistoryDto> getHistoryListByUser(final String userId, final Integer key) throws Exception {
        final JrnlDiaryDto diary = jrnlDiaryService.getDtlDtoWithCacheByUser(userId, key);
        return historyService.getHistoryList(diary.getClsfKey());
    }

    @Transactional
    public JrnlDiaryDto restoreHistoryByUser(final String userId, final Integer key, final Integer historyNo) throws Exception {
        final JrnlDiaryDto diary = jrnlDiaryService.getDtlDtoWithCacheByUser(userId, key);
        final Optional<HistoryDto> history = historyService.getHistory(diary.getClsfKey(), historyNo);
        if (history.isEmpty()) {
            throw new IllegalArgumentException("복구할 이력이 없습니다.");
        }

        final JrnlDiaryPostDto restoreDto = JrnlDiaryPostDto.builder()
                .postNo(diary.getPostNo())
                .title(diary.getTitle())
                .cn(history.get().getCn())
                .jrnlDayNo(diary.getJrnlDayNo())
                .jrnlEntryNo(diary.getJrnlEntryNo())
                .yy(diary.getYy())
                .mnth(diary.getMnth())
                .idx(diary.getIdx())
                .isIdxChanged(false)
                .isEntryChanged(false)
                .historyType(HistoryType.RESTORE.key)
                .fromHistoryNo(historyNo)
                .build();

        jrnlDiaryService.modify(restoreDto);
        return jrnlDiaryService.getDtlDtoWithCacheByUser(userId, key);
    }

    @Transactional
    public boolean deleteHistoryByUser(final String userId, final Integer key, final Integer historyNo) throws Exception {
        final JrnlDiaryDto diary = jrnlDiaryService.getDtlDtoWithCacheByUser(userId, key);
        return historyService.deleteHistory(diary.getClsfKey(), historyNo);
    }
}
