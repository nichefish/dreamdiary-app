package io.nicheblog.dreamdiary.feature.journal._shared.lifecycle;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.LifecycleKey;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.entity.LifecycleEntity;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.model.LifecycleSetDto;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.repository.jpa.LifecycleRepository;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.service.LifecycleService;
import io.nicheblog.dreamdiary.feature.attachable.state.repository.jpa.StateRepository;
import io.nicheblog.dreamdiary.feature.attachable.state.service.StateService;
import io.nicheblog.dreamdiary.feature.journal.day.service.helper.JournalDayResolvedGuard;
import io.nicheblog.dreamdiary.feature.journal.reflection.entity.JournalReflectionEntity;
import io.nicheblog.dreamdiary.feature.journal.reflection.repository.jpa.JournalReflectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * primary 완료 요청이 직접 연결된 미완료 Reflection 만 RESOLVED 로 수렴시키는 계약을 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class JournalReflectionLifecycleCascadeTest {

    private static final Integer FIXTURE_PARENT_ID = 101;
    private static final Integer FIXTURE_OPEN_REFLECTION_ID = 201;
    private static final Integer FIXTURE_PENDING_REFLECTION_ID = 202;
    private static final Integer FIXTURE_RESOLVED_REFLECTION_ID = 203;

    @Mock
    private JournalReflectionRepository journalReflectionRepository;
    @Mock
    private LifecycleRepository lifecycleRepository;
    @Mock
    private StateRepository stateRepository;
    @Mock
    private StateService stateService;
    @Mock
    private JournalDayResolvedGuard journalDayResolvedGuard;
    @Mock
    private LifecycleService lifecycleService;

    private JournalReflectionLifecycleCascade cascade;

    @BeforeEach
    void setUp() {
        cascade = new JournalReflectionLifecycleCascade(
                journalReflectionRepository,
                lifecycleRepository,
                stateRepository,
                stateService,
                journalDayResolvedGuard,
                lifecycleService
        );
    }

    /** OPEN·PENDING Reflection 만 RESOLVED 로 전환하고 이미 RESOLVED 인 Reflection 은 건너뛴다. */
    @Test
    void cascadeResolvedTransitionsOnlyUnresolvedReflections() throws Exception {
        when(journalReflectionRepository.findAllByRefIdAndRefContentType(
                FIXTURE_PARENT_ID,
                ContentType.JOURNAL_DIARY
        )).thenReturn(List.of(
                reflection(FIXTURE_OPEN_REFLECTION_ID),
                reflection(FIXTURE_PENDING_REFLECTION_ID),
                reflection(FIXTURE_RESOLVED_REFLECTION_ID)
        ));
        when(lifecycleRepository.findAllByRefContentTypeAndRefIdIn(
                eq(ContentType.JOURNAL_REFLECTION.key),
                any()
        )).thenReturn(List.of(
                lifecycle(FIXTURE_PENDING_REFLECTION_ID, LifecycleKey.PENDING),
                lifecycle(FIXTURE_RESOLVED_REFLECTION_ID, LifecycleKey.RESOLVED)
        ));

        cascade.cascadeResolvedToAttachedReflections(parentResolvedRequest());

        final ArgumentCaptor<LifecycleSetDto> requestCaptor = ArgumentCaptor.forClass(LifecycleSetDto.class);
        verify(lifecycleService, times(2)).set(requestCaptor.capture());
        final Set<Integer> transitionedIds = requestCaptor.getAllValues().stream()
                .map(LifecycleSetDto::getId)
                .collect(Collectors.toSet());
        assertEquals(Set.of(FIXTURE_OPEN_REFLECTION_ID, FIXTURE_PENDING_REFLECTION_ID), transitionedIds);
        requestCaptor.getAllValues().forEach(request -> {
            assertEquals(ContentType.JOURNAL_REFLECTION, request.getContentType());
            assertEquals(LifecycleKey.RESOLVED, request.getLifecycleKey());
        });
    }

    /** 직접 연결된 Reflection 이 모두 RESOLVED 이면 하위 lifecycle 후처리를 호출하지 않는다. */
    @Test
    void cascadeResolvedSkipsAllAlreadyResolvedReflections() {
        when(journalReflectionRepository.findAllByRefIdAndRefContentType(
                FIXTURE_PARENT_ID,
                ContentType.JOURNAL_DIARY
        )).thenReturn(List.of(
                reflection(FIXTURE_OPEN_REFLECTION_ID),
                reflection(FIXTURE_RESOLVED_REFLECTION_ID)
        ));
        when(lifecycleRepository.findAllByRefContentTypeAndRefIdIn(
                eq(ContentType.JOURNAL_REFLECTION.key),
                any()
        )).thenReturn(List.of(
                lifecycle(FIXTURE_OPEN_REFLECTION_ID, LifecycleKey.RESOLVED),
                lifecycle(FIXTURE_RESOLVED_REFLECTION_ID, LifecycleKey.RESOLVED)
        ));

        cascade.cascadeResolvedToAttachedReflections(parentResolvedRequest());

        verifyNoInteractions(lifecycleService);
    }

    private LifecycleSetDto parentResolvedRequest() {
        return LifecycleSetDto.builder()
                .id(FIXTURE_PARENT_ID)
                .contentType(ContentType.JOURNAL_DIARY)
                .lifecycleKey(LifecycleKey.RESOLVED)
                .build();
    }

    private JournalReflectionEntity reflection(final Integer id) {
        return JournalReflectionEntity.builder()
                .id(id)
                .refId(FIXTURE_PARENT_ID)
                .refContentType(ContentType.JOURNAL_DIARY)
                .build();
    }

    private LifecycleEntity lifecycle(final Integer refId, final LifecycleKey lifecycleKey) {
        return LifecycleEntity.builder()
                .refId(refId)
                .refContentType(ContentType.JOURNAL_REFLECTION.key)
                .lifecycleKey(lifecycleKey.key)
                .build();
    }
}
