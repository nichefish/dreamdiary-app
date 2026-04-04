package io.nicheblog.dreamdiary.feature.jrnl.diary.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.feature.jrnl.diary.service.JrnlDiaryHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyJrnlDiaryHistoryService {

    private final JrnlDiaryHistoryService jrnlDiaryHistoryService;

    public List<HistoryDto> getMyHistoryList(final Integer key) throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return jrnlDiaryHistoryService.getHistoryListByUser(userId, key);
    }

    public JrnlDiaryDto restoreMyHistory(final Integer key, final Integer historyNo) throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return jrnlDiaryHistoryService.restoreHistoryByUser(userId, key, historyNo);
    }

    public boolean deleteMyHistory(final Integer key, final Integer historyNo) throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return jrnlDiaryHistoryService.deleteHistoryByUser(userId, key, historyNo);
    }
}
