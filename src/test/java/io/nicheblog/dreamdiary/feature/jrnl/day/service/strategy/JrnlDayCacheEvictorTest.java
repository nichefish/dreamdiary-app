package io.nicheblog.dreamdiary.feature.jrnl.day.service.strategy;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.jrnl._shared.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.cache.interceptor.SimpleKey;

import static org.mockito.Mockito.mockStatic;

class JrnlDayCacheEvictorTest {

    @Test
    void evict_clearsCurrentMonthListCacheAndStateMaps() throws Exception {
        final JrnlDayCacheEvictor evictor = new JrnlDayCacheEvictor();
        final JrnlCacheEvictParam param = JrnlCacheEvictParam.builder()
                .postNo(101)
                .yy(2026)
                .mnth(3)
                .build();

        try (
                final MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class);
                final MockedStatic<EhCacheUtils> ehCacheUtils = mockStatic(EhCacheUtils.class)
        ) {
            authUtils.when(AuthUtils::getLgnUserId).thenReturn("nichefish");

            evictor.evict(param);

            ehCacheUtils.verify(() -> EhCacheUtils.evictMyCacheByKey("jrnlDayDtlDtoByUser", 101));
            ehCacheUtils.verify(() -> EhCacheUtils.evictCacheByKey(
                    "jrnlDayYyMnthListByUser",
                    new SimpleKey("nichefish", 2026, 3)
            ));
            ehCacheUtils.verify(() -> EhCacheUtils.evictCacheByKey(
                    "jrnlEntryStateMapByUser",
                    new SimpleKey("nichefish", 2026, 3)
            ));
            ehCacheUtils.verify(() -> EhCacheUtils.evictCacheByKey(
                    "jrnlDiaryStateMapByUser",
                    new SimpleKey("nichefish", 2026, 3)
            ));
            ehCacheUtils.verify(() -> EhCacheUtils.evictCacheByKey(
                    "jrnlDreamStateMapByUser",
                    new SimpleKey("nichefish", 2026, 3)
            ));
            ehCacheUtils.verify(() -> EhCacheUtils.evictCacheByKey(
                    "jrnlIntrptStateMapByUser",
                    new SimpleKey("nichefish", 2026, 3)
            ));
        }
    }
}
