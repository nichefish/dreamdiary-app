package io.nicheblog.dreamdiary.feature.jrnl._shared.adapter;

import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.day.service.JrnlDayMetaService;
import io.nicheblog.dreamdiary.feature.jrnl.day.service.JrnlDayService;
import io.nicheblog.dreamdiary.feature.jrnl.day.service.JrnlDayTagService;
import io.nicheblog.dreamdiary.feature.jrnl.diary.service.JrnlDiaryTagService;
import io.nicheblog.dreamdiary.feature.jrnl.dream.service.JrnlDreamTagService;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.port.CacheWarmupTask;
import io.nicheblog.dreamdiary.infrastructure.cache.port.LoginCacheWarmupTask;
import io.nicheblog.dreamdiary.infrastructure.cache.service.impl.EhCacheWarmupServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * JrnlCacheWarmupTask
 * <pre>
 *  저널 관련 캐시 워밍업 작업 구현체
 * </pre>
 *
 * @author nichefish
 * @see EhCacheWarmupServiceImpl
 */
@Component
@Order(10)
@RequiredArgsConstructor
public class JrnlCacheWarmupTask
        implements CacheWarmupTask, LoginCacheWarmupTask {

    private final JrnlDayService jrnlDayService;
    private final JrnlDayTagService jrnlDayTagService;
    private final JrnlDiaryTagService jrnlDiaryTagService;
    private final JrnlDreamTagService jrnlDreamTagService;
    private final JrnlDayMetaService jrnlDayMetaService;

    /**
     * 캐시 웜업
     */
    @Override
    public void warmup() throws Exception {
        // TODO: 사용자 기반 워밍업 필요
        jrnlDayTagService.getTagCtgrMap("nichefish");
        jrnlDiaryTagService.getTagCtgrMap("nichefish");
        jrnlDreamTagService.getTagCtgrMap("nichefish");

        // TODO: 사용자 기반 워밍업 필요
        jrnlDayMetaService.getMetaCtgrMap("nichefish");
    }

    /**
     * 로그인시 캐시 웜업
     * @param userId String
     */
    @Override
    public void warmupOnLgn(final String userId) throws Exception {
        final JrnlDaySearchParam param = JrnlDaySearchParam.builder()
                .yy(DateUtils.getCurrYy())
                .mnth(DateUtils.getCurrMnth())
                .build();
        // 로그인 시 해당 사용자의 월별 저널 목록(휴일 포함) 캐시 워밍업
        jrnlDayService.getMyListDtoByYyMnthWithHldy(userId, param);
    }
}
