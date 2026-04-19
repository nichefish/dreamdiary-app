package io.nicheblog.dreamdiary.feature.calendar.schedule.adapter;

import io.nicheblog.dreamdiary.feature.calendar.schedule.service.ScheduleService;
import io.nicheblog.dreamdiary.infrastructure.cache.port.CacheWarmupTask;
import io.nicheblog.dreamdiary.infrastructure.cache.service.impl.EhCacheWarmupServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * ScheduleCacheWarmupTask
 * <pre>
 *  일정(스케줄) 관련 캐시 워밍업 작업 구현체
 * </pre>
 *
 * @author nichefish
 * @see EhCacheWarmupServiceImpl
 */
@Component
@Order(20)
@RequiredArgsConstructor
public class ScheduleCacheWarmupTask
        implements CacheWarmupTask {

    private final ScheduleService scheduleService;

    /**
     * 워밍업 실행
     */
    @Override
    public void warmup() throws Exception {
        // 공휴일 정보를 다시 동기화하여 캐시에 갱신한다.
        scheduleService.resyncHolydayMap();
    }
}
