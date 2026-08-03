package io.nicheblog.dreamdiary.feature.journal.day.service.strategy;

import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.cache.interceptor.SimpleKey;

import static org.mockito.Mockito.mockStatic;

class JournalDayCacheEvictorTest {

    private static final String FIXTURE_USERNAME = "alice";

    @Test
    void evict_clearsCurrentMonthListCacheAndStateMaps() throws Exception {
        final JournalDayCacheEvictor evictor = new JournalDayCacheEvictor();
        final JournalCacheEvictParam param = JournalCacheEvictParam.builder()
                .id(101)
                .yy(2026)
                .mnth(3)
                .createdBy(FIXTURE_USERNAME)
                .build();

        try (
                final MockedStatic<EhCacheUtils> ehCacheUtils = mockStatic(EhCacheUtils.class)
        ) {
            evictor.evict(param);

            ehCacheUtils.verify(() -> EhCacheUtils.evictUserCacheByKey("journalDayDtlDtoByUser", FIXTURE_USERNAME, 101));
            ehCacheUtils.verify(() -> EhCacheUtils.evictCacheByKey(
                    "journalDayYyMnthListByUser",
                    new SimpleKey(FIXTURE_USERNAME, 2026, 3)
            ));
            ehCacheUtils.verify(() -> EhCacheUtils.evictCacheByKey(
                    "journalChapterStateMapByUser",
                    new SimpleKey(FIXTURE_USERNAME, 2026, 3)
            ));
            ehCacheUtils.verify(() -> EhCacheUtils.evictCacheByKey(
                    "journalDiaryStateMapByUser",
                    new SimpleKey(FIXTURE_USERNAME, 2026, 3)
            ));
            ehCacheUtils.verify(() -> EhCacheUtils.evictCacheByKey(
                    "journalDreamStateMapByUser",
                    new SimpleKey(FIXTURE_USERNAME, 2026, 3)
            ));
            ehCacheUtils.verify(() -> EhCacheUtils.evictCacheByKey(
                    "journalInterpretationStateMapByUser",
                    new SimpleKey(FIXTURE_USERNAME, 2026, 3)
            ));
        }
    }
}
