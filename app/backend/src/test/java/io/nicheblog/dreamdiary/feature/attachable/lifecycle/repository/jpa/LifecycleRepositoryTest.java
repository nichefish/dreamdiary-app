package io.nicheblog.dreamdiary.feature.attachable.lifecycle.repository.jpa;

import io.nicheblog.dreamdiary.feature.attachable.lifecycle.LifecycleKey;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.entity.LifecycleEntity;
import io.nicheblog.dreamdiary.global.config.DataSourceConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 라이프사이클 현재값 row의 물리 삭제와 유니크 키 재사용 계약 테스트.
 *
 * @author nichefish
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(DataSourceConfig.class)
@Import(io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig.class)
class LifecycleRepositoryTest {

    private static final Integer FIXTURE_REF_ID = -925001;
    private static final String FIXTURE_CONTENT_TYPE = "JOURNAL_DIARY";

    @Autowired
    private LifecycleRepository repository;

    /** OPEN 전환으로 row를 지운 뒤 같은 콘텐츠의 명시적 상태를 다시 저장할 수 있다. */
    @Test
    void deleteCurrentByRefReleasesUniqueKeyForNextLifecycle() {
        repository.saveAndFlush(lifecycle(LifecycleKey.PENDING));

        final int deletedCount = repository.deleteCurrentByRef(FIXTURE_REF_ID, FIXTURE_CONTENT_TYPE);

        assertEquals(1, deletedCount);
        assertTrue(repository.findByRefIdAndRefContentType(FIXTURE_REF_ID, FIXTURE_CONTENT_TYPE).isEmpty());

        repository.saveAndFlush(lifecycle(LifecycleKey.RESOLVED));

        assertEquals(
                LifecycleKey.RESOLVED.key,
                repository.findByRefIdAndRefContentType(FIXTURE_REF_ID, FIXTURE_CONTENT_TYPE)
                        .orElseThrow()
                        .getLifecycleKey()
        );
    }

    private LifecycleEntity lifecycle(final LifecycleKey lifecycleKey) {
        return LifecycleEntity.builder()
                .refId(FIXTURE_REF_ID)
                .refContentType(FIXTURE_CONTENT_TYPE)
                .lifecycleKey(lifecycleKey.key)
                .build();
    }
}
