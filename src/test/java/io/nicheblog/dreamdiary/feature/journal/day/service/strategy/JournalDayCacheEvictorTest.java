package io.nicheblog.dreamdiary.feature.journal.day.service.strategy;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.cache.interceptor.SimpleKey;

import static org.mockito.Mockito.mockStatic;

class JournalDayCacheEvictorTest {

    @Test
    void evict_clearsCurrentMonthListCacheAndStateMaps() throws Exception {
        final JournalDayCacheEvictor evictor = new JournalDayCacheEvictor();
        final JournalCacheEvictParam param = JournalCacheEvictParam.builder()
                .id(101)
                .yy(2026)
                .mnth(3)
                .build();

        try (
                final MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class);
                final MockedStatic<EhCacheUtils> ehCacheUtils = mockStatic(EhCacheUtils.class)
        ) {
            authUtils.when(AuthUtils::getLoginUsername).thenReturn("nichefish");

            evictor.evict(param);

            ehCacheUtils.verify(() -> EhCacheUtils.evictMyCacheByKey("journalDayDtlDtoByUser", 101));
            ehCacheUtils.verify(() -> EhCacheUtils.evictCacheByKey(
                    "journalDayYyMnthListByUser",
                    new SimpleKey("nichefish", 2026, 3)
            ));
            ehCacheUtils.verify(() -> EhCacheUtils.evictCacheByKey(
                    "journalChapterStateMapByUser",
                    new SimpleKey("nichefish", 2026, 3)
            ));
            ehCacheUtils.verify(() -> EhCacheUtils.evictCacheByKey(
                    "journalDiaryStateMapByUser",
                    new SimpleKey("nichefish", 2026, 3)
            ));
            ehCacheUtils.verify(() -> EhCacheUtils.evictCacheByKey(
                    "journalDreamStateMapByUser",
                    new SimpleKey("nichefish", 2026, 3)
            ));
            ehCacheUtils.verify(() -> EhCacheUtils.evictCacheByKey(
                    "journalIntrptStateMapByUser",
                    new SimpleKey("nichefish", 2026, 3)
            ));
        }
    }
}

