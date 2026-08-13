package io.nicheblog.dreamdiary.feature.journal._shared.lifecycle;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.entry.service.JournalEntryService;
import org.ehcache.xml.XmlConfiguration;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

class JournalLifecycleCacheConfigurationTest {

    @Test
    void registersMonthlyAndWeeklyCacheForEverySupportedContentType() {
        final URL configUrl = getClass().getClassLoader().getResource("config/ehcache/ehcache.xml");

        assertThat(configUrl).isNotNull();
        final XmlConfiguration configuration = new XmlConfiguration(configUrl);
        for (final ContentType contentType : JournalLifecycleCacheRegistry.lifecycleContentTypes()) {
            assertThat(configuration.getCacheConfigurations())
                    .containsKeys(
                            JournalLifecycleCacheRegistry.monthlyMapCacheName(contentType),
                            JournalLifecycleCacheRegistry.weeklyMapCacheName(contentType)
                    );
        }
    }

    @Test
    void annualLifecycleEvictionUsesActualAnnualEntryListCache() {
        assertThat(JournalLifecycleCacheRegistry.annualLifecycleListCacheName(ContentType.JOURNAL_DIARY))
                .isEqualTo(JournalEntryService.ANNUAL_STATED_LIST_CACHE_NAME);
        assertThat(JournalLifecycleCacheRegistry.annualLifecycleListCacheName(ContentType.JOURNAL_DREAM))
                .isEqualTo(JournalEntryService.ANNUAL_STATED_LIST_CACHE_NAME);
        assertThat(JournalLifecycleCacheRegistry.annualLifecycleListCacheName(ContentType.JOURNAL_REFLECTION))
                .isNull();
    }
}
