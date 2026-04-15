package io.nicheblog.dreamdiary.feature.journal._shared.adapter;

import io.nicheblog.dreamdiary.feature.journal.day.service.JournalDayMetaService;
import io.nicheblog.dreamdiary.feature.journal.day.service.JournalDayService;
import io.nicheblog.dreamdiary.feature.journal.day.service.JournalDayTagService;
import io.nicheblog.dreamdiary.feature.journal.diary.service.JournalDiaryTagService;
import io.nicheblog.dreamdiary.feature.journal.dream.service.JournalDreamTagService;
import io.nicheblog.dreamdiary.feature.journal.intrpt.service.JournalIntrptTagService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.port.CacheWarmupTask;
import io.nicheblog.dreamdiary.infrastructure.cache.port.LoginCacheWarmupTask;
import io.nicheblog.dreamdiary.infrastructure.cache.service.impl.EhCacheWarmupServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * JournalCacheWarmupTask
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
public class JournalCacheWarmupTask
        implements CacheWarmupTask, LoginCacheWarmupTask {

    private final JournalDayService journalDayService;
    private final JournalDayTagService journalDayTagService;
    private final JournalDiaryTagService journalDiaryTagService;
    private final JournalDreamTagService journalDreamTagService;
    private final JournalDayMetaService journalDayMetaService;
    private final JournalIntrptTagService journalIntrptTagService;

    /**
     * 캐시 웜업
     */
    @Override
    public void warmup() throws Exception {
        // 사용자 기반 워밍업
        journalDayTagService.getTagCtgrMapByUser(Constant.SYSTEM_ACNT);
        journalDayMetaService.getMetaCtgrMapByUser(Constant.SYSTEM_ACNT);
        journalDiaryTagService.getTagCtgrMapByUser(Constant.SYSTEM_ACNT);
        journalDreamTagService.getTagCtgrMapByUser(Constant.SYSTEM_ACNT);
        journalIntrptTagService.getTagCtgrMapByUser(Constant.SYSTEM_ACNT);
    }

    /**
     * 로그인시 캐시 웜업
     * @param username String
     */
    @Override
    public void warmupOnLgn(final String username) throws Exception {
        // 로그인 시 해당 사용자의 월별 저널 목록(휴일 포함) 캐시 워밍업
        journalDayService.getCachedYyMnthListDtoByUser(username, DateUtils.getCurrYy(), DateUtils.getCurrMnth());
    }
}
