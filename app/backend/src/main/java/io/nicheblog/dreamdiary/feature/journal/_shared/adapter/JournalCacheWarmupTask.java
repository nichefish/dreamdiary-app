package io.nicheblog.dreamdiary.feature.journal._shared.adapter;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.day.service.JournalDayMetaService;
import io.nicheblog.dreamdiary.feature.journal.day.service.JournalDayService;
import io.nicheblog.dreamdiary.feature.journal.day.service.JournalDayTagService;
import io.nicheblog.dreamdiary.feature.journal.entry.service.JournalEntryTagService;
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
    private final JournalEntryTagService journalEntryTagService;
    private final JournalDayMetaService journalDayMetaService;

    /**
     * 캐시 웜업
     */
    @Override
    public void warmup() throws Exception {
        journalDayTagService.getTagCtgrMapByUser(Constant.SYSTEM_ACNT);
        journalDayMetaService.getMetaCtgrMapByUser(Constant.SYSTEM_ACNT);
        journalEntryTagService.getTagCtgrMapByUser(Constant.SYSTEM_ACNT, ContentType.JOURNAL_DIARY);
        journalEntryTagService.getTagCtgrMapByUser(Constant.SYSTEM_ACNT, ContentType.JOURNAL_DREAM);
    }

    /**
     * 로그인시 캐시 웜업
     * @param username String
     */
    @Override
    public void warmupOnLogin(final String username) throws Exception {
        journalDayService.getCachedYyMnthListDtoByUser(username, DateUtils.getCurrYy(), DateUtils.getCurrMnth());
    }
}
