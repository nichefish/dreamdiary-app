package io.nicheblog.dreamdiary.feature.journal.dream.service.helper;

import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;
import io.nicheblog.dreamdiary.feature.journal.dream.model.JournalDreamDto;
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
 * 키워드 검색 등 플랫 꿈 목록에 월 단위 상태 캐시를 반영한다.
 */
@UtilityClass
public class JournalDreamSearchViewHelper {

    @SuppressWarnings("unchecked")
    public static void mergeStatesFromUserCaches(final String username, final List<JournalDreamDto> listDto) {
        if (CollectionUtils.isEmpty(listDto) || username == null) return;

        final Map<Object, List<JournalDreamDto>> byCacheKey = new HashMap<>();
        for (final JournalDreamDto dream : listDto) {
            if (dream == null || dream.getYy() == null || dream.getMnth() == null) continue;
            final Object cacheKey = new SimpleKey(username, dream.getYy(), dream.getMnth());
            byCacheKey.computeIfAbsent(cacheKey, k -> new ArrayList<>()).add(dream);
        }

        for (final Map.Entry<Object, List<JournalDreamDto>> e : byCacheKey.entrySet()) {
            final Object cacheKey = e.getKey();
            final Map<Integer, JournalState> dreamMap = Optional.ofNullable(
                    (Map<Integer, JournalState>) EhCacheUtils.getObjectFromCache("journalDreamStateMapByUser", cacheKey))
                    .orElse(Collections.emptyMap());
            final Map<Integer, JournalState> interpretationMap = Optional.ofNullable(
                    (Map<Integer, JournalState>) EhCacheUtils.getObjectFromCache("journalInterpretationStateMapByUser", cacheKey))
                    .orElse(Collections.emptyMap());
            JournalDreamViewHelper.applyStates(e.getValue(), dreamMap, interpretationMap);
        }
    }
}
