package io.nicheblog.dreamdiary.feature.journal.entry.service.helper;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalPeriodModule;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalStateCacheRegistry;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.interceptor.SimpleKey;

import java.util.*;

@UtilityClass
public class JournalSearchStateCacheHelper {

    @FunctionalInterface
    public interface MonthlyStateApplier<Dto> {
        /**
         * 월별 상태 맵을 DTO 목록에 반영한다.
         *
         * @param listDto 대상 목록
         * @param stateMap 상태 맵
         * @param interpretationMap 해석 상태 맵
         */
        void apply(List<Dto> listDto, Map<Integer, JournalState> stateMap, Map<Integer, JournalState> interpretationMap);
    }

    @SuppressWarnings("unchecked")
    /**
     * 사용자 월별 상태 캐시를 DTO 목록에 병합한다.
     *
     * @param username 사용자 아이디
     * @param listDto 대상 목록
     * @param stateCacheName 상태 캐시명
     * @param applier 병합 함수
     * @param <Dto> 저널 기간 DTO 타입
     */
    public static <Dto extends JournalPeriodModule> void mergeStatesFromUserCaches(
            final String username,
            final List<Dto> listDto,
            final String stateCacheName,
            final MonthlyStateApplier<Dto> applier
    ) {
        if (CollectionUtils.isEmpty(listDto) || username == null) return;

        final Map<Object, List<Dto>> byCacheKey = new HashMap<>();
        for (final Dto dto : listDto) {
            if (dto == null || dto.getYy() == null || dto.getMnth() == null) continue;
            final Object cacheKey = new SimpleKey(username, dto.getYy(), dto.getMnth());
            byCacheKey.computeIfAbsent(cacheKey, k -> new ArrayList<>()).add(dto);
        }

        for (final Map.Entry<Object, List<Dto>> entry : byCacheKey.entrySet()) {
            final Object cacheKey = entry.getKey();
            final Map<Integer, JournalState> stateMap = Optional.ofNullable(
                    (Map<Integer, JournalState>) EhCacheUtils.getObjectFromCache(stateCacheName, cacheKey)
            ).orElse(Collections.emptyMap());
            final Map<Integer, JournalState> interpretationMap = Optional.ofNullable(
                    (Map<Integer, JournalState>) EhCacheUtils.getObjectFromCache(
                            JournalStateCacheRegistry.monthlyMapCacheName(ContentType.JOURNAL_INTERPRETATION),
                            cacheKey
                    )
            ).orElse(Collections.emptyMap());
            applier.apply(entry.getValue(), stateMap, interpretationMap);
        }
    }
}
