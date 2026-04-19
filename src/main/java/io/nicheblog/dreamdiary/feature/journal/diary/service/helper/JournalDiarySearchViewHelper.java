package io.nicheblog.dreamdiary.feature.journal.diary.service.helper;

import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;
import io.nicheblog.dreamdiary.feature.journal.day.service.helper.JournalDayViewHelper;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryDto;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.interceptor.SimpleKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 키워드/태그 검색 등 플랫 일기 목록에 월 단위 상태 캐시를 반영한다.
 *
 * @see JournalDayViewHelper#mergeStates(String, List, io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam)
 */
@UtilityClass
public class JournalDiarySearchViewHelper {

    @SuppressWarnings("unchecked")
    public static void mergeStatesFromUserCaches(final String username, final List<JournalDiaryDto> listDto) {
        if (CollectionUtils.isEmpty(listDto) || username == null) return;

        final Map<Object, List<JournalDiaryDto>> byCacheKey = new HashMap<>();
        for (final JournalDiaryDto diary : listDto) {
            if (diary == null || diary.getYy() == null || diary.getMnth() == null) continue;
            final Object cacheKey = new SimpleKey(username, diary.getYy(), diary.getMnth());
            byCacheKey.computeIfAbsent(cacheKey, k -> new ArrayList<>()).add(diary);
        }

        for (final Map.Entry<Object, List<JournalDiaryDto>> e : byCacheKey.entrySet()) {
            final Object cacheKey = e.getKey();
            final Map<Integer, JournalState> diaryMap = Optional.ofNullable(
                    (Map<Integer, JournalState>) EhCacheUtils.getObjectFromCache("journalDiaryStateMapByUser", cacheKey))
                    .orElse(Collections.emptyMap());
            final Map<Integer, JournalState> interpretationMap = Optional.ofNullable(
                    (Map<Integer, JournalState>) EhCacheUtils.getObjectFromCache("journalInterpretationStateMapByUser", cacheKey))
                    .orElse(Collections.emptyMap());
            JournalDiaryViewHelper.applyStates(e.getValue(), diaryMap, interpretationMap);
        }
    }
}
