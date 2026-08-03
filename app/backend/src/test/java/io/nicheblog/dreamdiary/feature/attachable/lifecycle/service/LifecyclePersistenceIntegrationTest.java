package io.nicheblog.dreamdiary.feature.attachable.lifecycle.service;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.LifecycleKey;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.entity.LifecycleEntity;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.model.LifecycleSetDto;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.repository.jpa.LifecycleRepository;
import io.nicheblog.dreamdiary.feature.attachable.state.StateKey;
import io.nicheblog.dreamdiary.feature.attachable.state.entity.StateEntity;
import io.nicheblog.dreamdiary.feature.attachable.state.repository.jpa.StateRepository;
import io.nicheblog.dreamdiary.feature.journal._shared.security.JournalContentOwnershipGuard;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 라이프사이클 서비스의 현재값·파생 상태 실제 영속 계약을 검증한다.
 *
 * @author nichefish
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestAuditConfig.class)
@Transactional
class LifecyclePersistenceIntegrationTest {

    private static final ContentType FIXTURE_CONTENT_TYPE = ContentType.JOURNAL_DIARY;
    private static final Integer FIXTURE_PENDING_REF_ID = -926101;
    private static final Integer FIXTURE_OPEN_REF_ID = -926102;
    private static final Integer FIXTURE_RESOLVED_REF_ID = -926103;
    private static final Integer FIXTURE_PENDING_AFTER_RESOLVED_REF_ID = -926104;
    private static final Integer FIXTURE_REPEATED_RESOLVED_REF_ID = -926105;

    @Resource
    private LifecycleService lifecycleService;
    @Resource
    private LifecycleRepository lifecycleRepository;
    @Resource
    private StateRepository stateRepository;
    /** 이 테스트는 lifecycle/state 영속 전이에 집중하고 원본 소유권 계약은 전용 가드 테스트에 위임한다. */
    @MockBean
    private JournalContentOwnershipGuard journalContentOwnershipGuard;
    @PersistenceContext
    private EntityManager entityManager;

    /** PENDING은 명시적 현재값 row로 저장되고 목록용 맵에도 노출된다. */
    @Test
    void pendingPersistsAndAppearsInLifecycleMap() throws Exception {
        lifecycleService.set(request(FIXTURE_PENDING_REF_ID, LifecycleKey.PENDING));
        entityManager.flush();
        entityManager.clear();

        final LifecycleEntity stored = requireLifecycle(FIXTURE_PENDING_REF_ID);
        final Map<Integer, String> lifecycleMap = lifecycleService.getLifecycleMap(
                FIXTURE_CONTENT_TYPE,
                List.of(FIXTURE_PENDING_REF_ID)
        );

        assertEquals(LifecycleKey.PENDING.key, stored.getLifecycleKey());
        assertEquals(LifecycleKey.PENDING.key, lifecycleMap.get(FIXTURE_PENDING_REF_ID));
    }

    /** OPEN 전환은 현재값 row를 물리 삭제하고 같은 유니크 키의 다음 상태 저장을 허용한다. */
    @Test
    void openDeletesCurrentRowAndAllowsNextExplicitLifecycle() throws Exception {
        lifecycleService.set(request(FIXTURE_OPEN_REF_ID, LifecycleKey.PENDING));
        entityManager.flush();

        lifecycleService.set(request(FIXTURE_OPEN_REF_ID, LifecycleKey.OPEN));
        entityManager.flush();
        entityManager.clear();

        assertTrue(lifecycleRepository.findByRefIdAndRefContentType(
                FIXTURE_OPEN_REF_ID,
                FIXTURE_CONTENT_TYPE.key
        ).isEmpty());
        assertTrue(lifecycleService.getLifecycleMap(
                FIXTURE_CONTENT_TYPE,
                List.of(FIXTURE_OPEN_REF_ID)
        ).isEmpty());

        lifecycleService.set(request(FIXTURE_OPEN_REF_ID, LifecycleKey.RESOLVED));
        entityManager.flush();
        entityManager.clear();

        assertEquals(LifecycleKey.RESOLVED.key, requireLifecycle(FIXTURE_OPEN_REF_ID).getLifecycleKey());
    }

    /** RESOLVED는 현재값 row와 파생 COLLAPSED 상태를 함께 저장한다. */
    @Test
    void resolvedPersistsLifecycleAndDerivedCollapsedState() throws Exception {
        lifecycleService.set(request(FIXTURE_RESOLVED_REF_ID, LifecycleKey.RESOLVED));
        entityManager.flush();
        entityManager.clear();

        assertEquals(LifecycleKey.RESOLVED.key, requireLifecycle(FIXTURE_RESOLVED_REF_ID).getLifecycleKey());
        assertNotNull(requireCollapsedState(FIXTURE_RESOLVED_REF_ID));
    }

    /** RESOLVED에서 PENDING으로 돌아가도 표시 선호인 기존 COLLAPSED 상태는 유지한다. */
    @Test
    void pendingAfterResolvedKeepsDerivedCollapsedState() throws Exception {
        lifecycleService.set(request(FIXTURE_PENDING_AFTER_RESOLVED_REF_ID, LifecycleKey.RESOLVED));
        entityManager.flush();

        lifecycleService.set(request(FIXTURE_PENDING_AFTER_RESOLVED_REF_ID, LifecycleKey.PENDING));
        entityManager.flush();
        entityManager.clear();

        assertEquals(
                LifecycleKey.PENDING.key,
                requireLifecycle(FIXTURE_PENDING_AFTER_RESOLVED_REF_ID).getLifecycleKey()
        );
        assertNotNull(requireCollapsedState(FIXTURE_PENDING_AFTER_RESOLVED_REF_ID));
    }

    /** RESOLVED를 반복 설정해도 기존 COLLAPSED 상태를 재사용한다. */
    @Test
    void repeatedResolvedDoesNotDuplicateDerivedCollapsedState() throws Exception {
        lifecycleService.set(request(FIXTURE_REPEATED_RESOLVED_REF_ID, LifecycleKey.RESOLVED));
        entityManager.flush();
        final Integer originalStateId = requireCollapsedState(FIXTURE_REPEATED_RESOLVED_REF_ID).getId();

        lifecycleService.set(request(FIXTURE_REPEATED_RESOLVED_REF_ID, LifecycleKey.RESOLVED));
        entityManager.flush();
        entityManager.clear();

        assertEquals(originalStateId, requireCollapsedState(FIXTURE_REPEATED_RESOLVED_REF_ID).getId());
        assertEquals(LifecycleKey.RESOLVED.key, requireLifecycle(FIXTURE_REPEATED_RESOLVED_REF_ID).getLifecycleKey());
    }

    private LifecycleSetDto request(final Integer refId, final LifecycleKey lifecycleKey) {
        return LifecycleSetDto.builder()
                .id(refId)
                .contentType(FIXTURE_CONTENT_TYPE)
                .lifecycleKey(lifecycleKey)
                .build();
    }

    private LifecycleEntity requireLifecycle(final Integer refId) {
        return lifecycleRepository.findByRefIdAndRefContentType(refId, FIXTURE_CONTENT_TYPE.key)
                .orElseThrow();
    }

    private StateEntity requireCollapsedState(final Integer refId) {
        return stateRepository.findByRefIdAndRefContentTypeAndStateKey(
                refId,
                FIXTURE_CONTENT_TYPE.key,
                StateKey.COLLAPSED.key
        );
    }
}
