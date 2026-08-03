package io.nicheblog.dreamdiary.feature.attachable.prefix.service;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixContentEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.repository.jpa.PrefixContentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PERSONAL/GLOBAL Scope 검증과 prefix_content 단일 선택 반영 계약 테스트.
 *
 * @author nichefish
 */
@ExtendWith(MockitoExtension.class)
class PrefixContentServiceTest {

    private static final Integer FIXTURE_REF_ID = 101;
    private static final Integer FIXTURE_PREFIX_ID = 201;
    private static final String FIXTURE_PERSONAL_TYPE = "JOURNAL_THREAD";
    private static final String FIXTURE_GLOBAL_TYPE = "FIXTURE_BOARD";
    private static final String FIXTURE_GLOBAL_CONTEXT = "board-post:101:FIXTURE_BOARD";

    @Mock
    private PrefixContentRepository repository;
    @Mock
    private PrefixService prefixService;
    @InjectMocks
    private PrefixContentService service;

    @Test
    void applySelectionUsesPersonalScopeValidationAndCreatesConnection() {
        final BaseAttachableKey key = new BaseAttachableKey(FIXTURE_REF_ID, FIXTURE_PERSONAL_TYPE);
        final PrefixEntity prefix = PrefixEntity.builder().id(FIXTURE_PREFIX_ID).build();
        when(repository.findByRefIdAndRefContentType(FIXTURE_REF_ID, FIXTURE_PERSONAL_TYPE))
                .thenReturn(Optional.empty());
        when(prefixService.requireSelectable(FIXTURE_PERSONAL_TYPE, FIXTURE_PREFIX_ID, null))
                .thenReturn(prefix);

        final PrefixEntity result = service.applySelection(key, FIXTURE_PERSONAL_TYPE, FIXTURE_PREFIX_ID);

        assertSame(prefix, result);
        verify(repository).save(argThat(connection ->
                FIXTURE_REF_ID.equals(connection.getRefId())
                        && FIXTURE_PERSONAL_TYPE.equals(connection.getRefContentType())
                        && FIXTURE_PREFIX_ID.equals(connection.getPrefixId())
        ));
    }

    /** NOTE 목록 Scope와 기존 JOURNAL_DIARY 영속 타입을 각각의 계약으로 전달한다. */
    @Test
    void applySelectionKeepsScopeTypeSeparateFromReferenceType() {
        final String noteScopeType = "JOURNAL_NOTE";
        final String persistedReferenceType = "JOURNAL_DIARY";
        final BaseAttachableKey key = new BaseAttachableKey(FIXTURE_REF_ID, persistedReferenceType);
        final PrefixEntity prefix = PrefixEntity.builder().id(FIXTURE_PREFIX_ID).build();
        when(repository.findByRefIdAndRefContentType(FIXTURE_REF_ID, persistedReferenceType))
                .thenReturn(Optional.empty());
        when(prefixService.requireSelectable(noteScopeType, FIXTURE_PREFIX_ID, null))
                .thenReturn(prefix);

        service.applySelection(key, noteScopeType, FIXTURE_PREFIX_ID);

        verify(prefixService).requireSelectable(noteScopeType, FIXTURE_PREFIX_ID, null);
        verify(repository).save(argThat(connection ->
                FIXTURE_REF_ID.equals(connection.getRefId())
                        && persistedReferenceType.equals(connection.getRefContentType())
                        && FIXTURE_PREFIX_ID.equals(connection.getPrefixId())
        ));
    }

    /** 다른 Prefix를 선택하면 기존 연결 행의 Prefix만 교체한다. */
    @Test
    void applySelectionUpdatesExistingConnectionWhenPrefixChanges() {
        final Integer requestedPrefixId = 202;
        final BaseAttachableKey key = new BaseAttachableKey(FIXTURE_REF_ID, FIXTURE_PERSONAL_TYPE);
        final PrefixContentEntity existing = PrefixContentEntity.builder()
                .id(303)
                .prefixId(FIXTURE_PREFIX_ID)
                .refId(FIXTURE_REF_ID)
                .refContentType(FIXTURE_PERSONAL_TYPE)
                .build();
        final PrefixEntity requestedPrefix = PrefixEntity.builder().id(requestedPrefixId).build();
        when(repository.findByRefIdAndRefContentType(FIXTURE_REF_ID, FIXTURE_PERSONAL_TYPE))
                .thenReturn(Optional.of(existing));
        when(prefixService.requireSelectable(FIXTURE_PERSONAL_TYPE, requestedPrefixId, FIXTURE_PREFIX_ID))
                .thenReturn(requestedPrefix);

        final PrefixEntity result = service.applySelection(key, FIXTURE_PERSONAL_TYPE, requestedPrefixId);

        assertSame(requestedPrefix, result);
        verify(repository).save(existing);
        verify(repository, never()).delete(any());
        assertEquals(requestedPrefixId, existing.getPrefixId());
    }

    /** 선택 연결이 없는 콘텐츠의 선택 해제는 영속 변경 없이 끝난다. */
    @Test
    void applySelectionClearsMissingConnectionWithoutPersistence() {
        final BaseAttachableKey key = new BaseAttachableKey(FIXTURE_REF_ID, FIXTURE_PERSONAL_TYPE);
        when(repository.findByRefIdAndRefContentType(FIXTURE_REF_ID, FIXTURE_PERSONAL_TYPE))
                .thenReturn(Optional.empty());
        when(prefixService.requireSelectable(FIXTURE_PERSONAL_TYPE, null, null)).thenReturn(null);

        final PrefixEntity result = service.applySelection(key, FIXTURE_PERSONAL_TYPE, null);

        assertNull(result);
        verify(repository, never()).save(any());
        verify(repository, never()).delete(any());
    }

    @Test
    void applyGlobalSelectionUsesCurrentConnectionForInactiveKeepValidation() {
        final BaseAttachableKey key = new BaseAttachableKey(FIXTURE_REF_ID, FIXTURE_GLOBAL_TYPE);
        final PrefixContentEntity existing = PrefixContentEntity.builder()
                .id(301)
                .prefixId(FIXTURE_PREFIX_ID)
                .refId(FIXTURE_REF_ID)
                .refContentType(FIXTURE_GLOBAL_TYPE)
                .build();
        final PrefixEntity prefix = PrefixEntity.builder().id(FIXTURE_PREFIX_ID).build();
        when(repository.findByRefIdAndRefContentType(FIXTURE_REF_ID, FIXTURE_GLOBAL_TYPE))
                .thenReturn(Optional.of(existing));
        when(prefixService.requireSelectableGlobal(
                FIXTURE_GLOBAL_TYPE,
                FIXTURE_PREFIX_ID,
                FIXTURE_PREFIX_ID,
                FIXTURE_GLOBAL_CONTEXT
        )).thenReturn(prefix);

        final PrefixEntity result = service.applyGlobalSelection(
                key, FIXTURE_GLOBAL_TYPE, FIXTURE_PREFIX_ID, FIXTURE_GLOBAL_CONTEXT
        );

        assertSame(prefix, result);
        verify(repository, never()).save(any());
        verify(repository, never()).delete(any());
    }

    @Test
    void applyGlobalSelectionDeletesConnectionWhenSelectionCleared() {
        final BaseAttachableKey key = new BaseAttachableKey(FIXTURE_REF_ID, FIXTURE_GLOBAL_TYPE);
        final PrefixContentEntity existing = PrefixContentEntity.builder()
                .id(302)
                .prefixId(FIXTURE_PREFIX_ID)
                .refId(FIXTURE_REF_ID)
                .refContentType(FIXTURE_GLOBAL_TYPE)
                .build();
        when(repository.findByRefIdAndRefContentType(FIXTURE_REF_ID, FIXTURE_GLOBAL_TYPE))
                .thenReturn(Optional.of(existing));
        when(prefixService.requireSelectableGlobal(
                FIXTURE_GLOBAL_TYPE,
                null,
                FIXTURE_PREFIX_ID,
                FIXTURE_GLOBAL_CONTEXT
        )).thenReturn(null);

        service.applyGlobalSelection(key, FIXTURE_GLOBAL_TYPE, null, FIXTURE_GLOBAL_CONTEXT);

        verify(repository).delete(existing);
        verify(repository, never()).save(any());
    }
}
