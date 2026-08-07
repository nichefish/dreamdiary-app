package io.nicheblog.dreamdiary.feature.attachable.lifecycle.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.LifecycleKey;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.entity.LifecycleEntity;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.model.LifecycleSetDto;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.repository.jpa.LifecycleRepository;
import io.nicheblog.dreamdiary.feature.attachable.state.StateKey;
import io.nicheblog.dreamdiary.feature.attachable.state.entity.StateEntity;
import io.nicheblog.dreamdiary.feature.attachable.state.repository.jpa.StateRepository;
import io.nicheblog.dreamdiary.feature.attachable.state.service.StateService;
import io.nicheblog.dreamdiary.feature.journal._shared.security.JournalContentOwnershipGuard;
import io.nicheblog.dreamdiary.feature.journal.day.service.helper.JournalDayResolvedGuard;
import io.nicheblog.dreamdiary.feature.journal._shared.lifecycle.JournalReflectionLifecycleCascade;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 라이프사이클 기본 상태와 명시적 상태의 저장 계약 테스트.
 */
@ExtendWith(MockitoExtension.class)
class LifecycleServiceTest {

    private static final Integer FIXTURE_CONTENT_ID = 101;
    private static final ContentType FIXTURE_CONTENT_TYPE = ContentType.JOURNAL_DIARY;

    @Mock
    private LifecycleRepository repository;
    @Mock
    private StateRepository stateRepository;
    @Mock
    private StateService stateService;
    @Mock
    private JournalContentOwnershipGuard journalContentOwnershipGuard;
    @Mock
    private JournalDayResolvedGuard journalDayResolvedGuard;
    @Mock
    private JournalReflectionLifecycleCascade journalReflectionLifecycleCascade;

    private LifecycleService service;

    @BeforeEach
    void setUp() {
        service = new LifecycleService(
                repository,
                stateRepository,
                stateService,
                List.of(),
                journalContentOwnershipGuard,
                journalDayResolvedGuard,
                journalReflectionLifecycleCascade
        );
    }

    /** 저장 row가 없는 컨텐츠를 OPEN으로 설정해도 row를 만들지 않는다. */
    @Test
    void setOpenWithoutStoredLifecycleKeepsStorageEmpty() throws Exception {
        when(repository.findByRefIdAndRefContentType(FIXTURE_CONTENT_ID, FIXTURE_CONTENT_TYPE.key))
                .thenReturn(Optional.empty());

        final ServiceResponse result = service.set(request(LifecycleKey.OPEN));

        assertTrue(result.getRslt());
        assertLifecycleResult(result, "OPEN", "OPEN");
        verify(repository).deleteCurrentByRef(FIXTURE_CONTENT_ID, FIXTURE_CONTENT_TYPE.key);
        verify(repository, never()).save(any());
    }

    /** 명시적 상태에서 OPEN으로 돌아가면 저장 row를 물리 삭제한다. */
    @Test
    void setOpenDeletesStoredLifecycle() throws Exception {
        when(repository.findByRefIdAndRefContentType(FIXTURE_CONTENT_ID, FIXTURE_CONTENT_TYPE.key))
                .thenReturn(Optional.of(stored(LifecycleKey.PENDING)));
        when(repository.deleteCurrentByRef(FIXTURE_CONTENT_ID, FIXTURE_CONTENT_TYPE.key)).thenReturn(1);

        final ServiceResponse result = service.set(request(LifecycleKey.OPEN));

        assertTrue(result.getRslt());
        assertLifecycleResult(result, "PENDING", "OPEN");
        verify(repository).deleteCurrentByRef(FIXTURE_CONTENT_ID, FIXTURE_CONTENT_TYPE.key);
        verify(repository, never()).save(any());
    }

    /** PENDING은 명시적 상태이므로 저장 row를 생성한다. */
    @Test
    void setPendingPersistsLifecycle() throws Exception {
        when(repository.findByRefIdAndRefContentType(FIXTURE_CONTENT_ID, FIXTURE_CONTENT_TYPE.key))
                .thenReturn(Optional.empty());

        final ServiceResponse result = service.set(request(LifecycleKey.PENDING));

        assertTrue(result.getRslt());
        assertLifecycleResult(result, "OPEN", "PENDING");
        verify(repository).save(any(LifecycleEntity.class));
        verify(repository, never()).deleteCurrentByRef(any(), any());
    }

    /** RESOLVED는 라이프사이클 row와 파생 COLLAPSED 상태를 함께 생성한다. */
    @Test
    void setResolvedPersistsLifecycleAndCreatesCollapsedState() throws Exception {
        when(repository.findByRefIdAndRefContentType(FIXTURE_CONTENT_ID, FIXTURE_CONTENT_TYPE.key))
                .thenReturn(Optional.empty());
        when(stateRepository.findByRefIdAndRefContentTypeAndStateKey(
                FIXTURE_CONTENT_ID,
                FIXTURE_CONTENT_TYPE.key,
                StateKey.COLLAPSED.key
        )).thenReturn(null);

        final ServiceResponse result = service.set(request(LifecycleKey.RESOLVED));

        assertTrue(result.getRslt());
        assertLifecycleResult(result, "OPEN", "RESOLVED");
        verify(repository).save(argThat(entity -> LifecycleKey.RESOLVED.key.equals(entity.getLifecycleKey())));
        verify(stateRepository).save(argThat(entity ->
                FIXTURE_CONTENT_ID.equals(entity.getRefId())
                        && FIXTURE_CONTENT_TYPE.key.equals(entity.getRefContentType())
                        && StateKey.COLLAPSED.key.equals(entity.getStateKey())
        ));
    }

    /** 이미 COLLAPSED인 콘텐츠를 RESOLVED로 바꿔도 파생 상태를 중복 생성하지 않는다. */
    @Test
    void setResolvedKeepsExistingCollapsedStateWithoutDuplicate() throws Exception {
        when(repository.findByRefIdAndRefContentType(FIXTURE_CONTENT_ID, FIXTURE_CONTENT_TYPE.key))
                .thenReturn(Optional.empty());
        when(stateRepository.findByRefIdAndRefContentTypeAndStateKey(
                FIXTURE_CONTENT_ID,
                FIXTURE_CONTENT_TYPE.key,
                StateKey.COLLAPSED.key
        )).thenReturn(StateEntity.builder()
                .id(11)
                .refId(FIXTURE_CONTENT_ID)
                .refContentType(FIXTURE_CONTENT_TYPE.key)
                .stateKey(StateKey.COLLAPSED.key)
                .build());

        final ServiceResponse result = service.set(request(LifecycleKey.RESOLVED));

        assertTrue(result.getRslt());
        verify(stateRepository, never()).save(any());
    }

    /** PENDING 전환은 기존 COLLAPSED 상태를 생성하거나 해제하지 않는다. */
    @Test
    void setPendingDoesNotMutateCollapsedState() throws Exception {
        final LifecycleEntity lifecycle = stored(LifecycleKey.RESOLVED);
        when(repository.findByRefIdAndRefContentType(FIXTURE_CONTENT_ID, FIXTURE_CONTENT_TYPE.key))
                .thenReturn(Optional.of(lifecycle));

        final ServiceResponse result = service.set(request(LifecycleKey.PENDING));

        assertTrue(result.getRslt());
        assertLifecycleResult(result, "RESOLVED", "PENDING");
        assertEquals(LifecycleKey.PENDING.key, lifecycle.getLifecycleKey());
        verify(stateRepository, never()).findByRefIdAndRefContentTypeAndStateKey(any(), any(), any());
        verify(stateRepository, never()).save(any());
        verify(stateService, never()).doCache(any(), anyBoolean());
    }

    /** 원본 콘텐츠 소유권 검증 실패는 lifecycle·state 저장 전에 전파한다. */
    @Test
    void setRejectsUnownedContentBeforePersistence() throws Exception {
        doThrow(new NotAuthorizedException("common.result.access-not-authorized"))
                .when(journalContentOwnershipGuard)
                .assertOwned(FIXTURE_CONTENT_ID, FIXTURE_CONTENT_TYPE);

        assertThrows(NotAuthorizedException.class, () -> service.set(request(LifecycleKey.PENDING)));

        verifyNoInteractions(repository, stateRepository, stateService, journalDayResolvedGuard);
    }

    /** 라이프사이클 미지원 콘텐츠 타입은 저장과 쓰기 잠금 검사 전에 거절한다. */
    @Test
    void setRejectsUnsupportedContentTypeWithoutPersistence() throws Exception {
        final LifecycleSetDto unsupported = LifecycleSetDto.builder()
                .id(FIXTURE_CONTENT_ID)
                .contentType(ContentType.BOARD)
                .lifecycleKey(LifecycleKey.PENDING)
                .build();

        final ServiceResponse result = service.set(unsupported);

        assertFalse(result.getRslt());
        verifyNoInteractions(
                repository,
                stateRepository,
                stateService,
                journalContentOwnershipGuard,
                journalDayResolvedGuard
        );
    }

    /** 필수 라이프사이클 값이 없으면 영속 경로에 진입하지 않는다. */
    @Test
    void setRejectsMissingLifecycleKeyWithoutPersistence() throws Exception {
        final LifecycleSetDto missingLifecycle = LifecycleSetDto.builder()
                .id(FIXTURE_CONTENT_ID)
                .contentType(FIXTURE_CONTENT_TYPE)
                .build();

        final ServiceResponse result = service.set(missingLifecycle);

        assertFalse(result.getRslt());
        verifyNoInteractions(
                repository,
                stateRepository,
                stateService,
                journalContentOwnershipGuard,
                journalDayResolvedGuard
        );
    }

    private LifecycleSetDto request(final LifecycleKey lifecycleKey) {
        return LifecycleSetDto.builder()
                .id(FIXTURE_CONTENT_ID)
                .contentType(FIXTURE_CONTENT_TYPE)
                .lifecycleKey(lifecycleKey)
                .build();
    }

    private LifecycleEntity stored(final LifecycleKey lifecycleKey) {
        return LifecycleEntity.builder()
                .id(1)
                .refId(FIXTURE_CONTENT_ID)
                .refContentType(FIXTURE_CONTENT_TYPE.key)
                .lifecycleKey(lifecycleKey.key)
                .build();
    }

    @SuppressWarnings("unchecked")
    private void assertLifecycleResult(
            final ServiceResponse result,
            final String previousLifecycleKey,
            final String currentLifecycleKey
    ) {
        final Map<String, String> lifecycleResult = (Map<String, String>) result.getRsltObj();
        assertEquals(previousLifecycleKey, lifecycleResult.get("previousLifecycleKey"));
        assertEquals(currentLifecycleKey, lifecycleResult.get("currentLifecycleKey"));
    }
}
