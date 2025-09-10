package io.nicheblog.dreamdiary.extension.cache.service.impl;

import io.nicheblog.dreamdiary.DreamdiaryInitializer;
import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.domain.jrnl.day.service.JrnlDayService;
import io.nicheblog.dreamdiary.domain.jrnl.day.service.JrnlDayTagService;
import io.nicheblog.dreamdiary.domain.jrnl.diary.service.JrnlDiaryTagService;
import io.nicheblog.dreamdiary.domain.jrnl.dream.service.JrnlDreamTagService;
import io.nicheblog.dreamdiary.domain.schdul.service.SchdulService;
import io.nicheblog.dreamdiary.extension.cache.service.CacheWarmupService;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * CacheWarmupService
 * <pre>
 *  캐시 웜업 서비스 모듈
 * </pre>
 *
 * @author nichefish
 * @see DreamdiaryInitializer
 */
@Service("cacheWarmupService")
@RequiredArgsConstructor
@Log4j2
public class CacheWarmupServiceImpl
        implements CacheWarmupService {

    @Resource(name="jCacheManager")
    @SuppressWarnings("unused")
    private CacheManager cacheManager;

    private final JrnlDayService jrnlDayService;
    private final JrnlDayTagService jrnlDayTagService;
    private final JrnlDiaryTagService jrnlDiaryTagService;
    private final JrnlDreamTagService jrnlDreamTagService;
    private final SchdulService schdulService;

    /**
     * 캐시 웜업
     */
    public void warmup() throws Exception {
        // 태그 카테고리 맵 캐시 웜업
        this.warmupTagCtgrMap();
        // 공휴일 맵 캐시 웜업
        this.warmupHldyMap();
    }

    /**
     * 태그 카테고리 맵 캐시 웜업
     */
    @Override
    public void warmupTagCtgrMap() throws Exception {
        // TODO: 사용자별 캐시 웜업
        jrnlDayTagService.getTagCtgrMap("nichefish");
        jrnlDiaryTagService.getTagCtgrMap("nichefish");
        jrnlDreamTagService.getTagCtgrMap("nichefish");
    }

    /**
     * 공휴일 맵 캐시 웜업
     */
    @Override
    public void warmupHldyMap() throws Exception {
        schdulService.resyncHldyMap();
    }

    /**
     * 로그인시 캐시 웜업
     */
    @Override
    public void warmupOnLgn(final String userId) throws Exception {
        final JrnlDaySearchParam param = JrnlDaySearchParam.builder()
                .yy(DateUtils.getCurrYy())
                .mnth(DateUtils.getCurrMnth())
                .build();
        jrnlDayService.getMyListDto(userId, param);
    }
}
