package io.nicheblog.dreamdiary.feature.journal.thread.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable.prefix.service.PrefixContentService;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.service.LifecycleService;
import io.nicheblog.dreamdiary.feature.journal.entry.service.JournalEntryService;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadCandidateDto;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadCandidateProjection;
import io.nicheblog.dreamdiary.feature.journal.thread.repository.jpa.JournalThreadRepository;
import io.nicheblog.dreamdiary.feature.journal.thread.spec.JournalThreadSpec;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 저널 스레드 후보 조회 서비스 계약 검증.
 * <p>
 * 사용자 범위, 검색값 정규화, 조회 상한과 경량 DTO 변환을 고정한다.
 */
@ExtendWith(MockitoExtension.class)
class JournalThreadCandidateServiceTest {

    private static final String FIXTURE_USERNAME = "fixture_user";
    private static final int FIXTURE_ENTRY_ID = 20;

    @Mock
    private JournalThreadRepository repository;
    @Mock
    private JournalThreadEntryService journalThreadEntryService;
    @Mock
    private JournalEntryService journalEntryService;
    @Mock
    private LifecycleService lifecycleService;
    @Mock
    private JournalThreadSpec spec;
    @Mock
    private ApplicationEventPublisherWrapper publisher;
    @Mock
    private PrefixContentService prefixContentService;

    @InjectMocks
    private JournalThreadService service;

    private MockedStatic<AuthUtils> authUtils;

    /** 인증 사용자를 공통 픽스처로 준비한다. */
    @BeforeEach
    void setUp() {
        authUtils = mockStatic(AuthUtils.class);
        authUtils.when(AuthUtils::requireLoginUsername).thenReturn(FIXTURE_USERNAME);
    }

    /** 정적 인증 mock을 테스트마다 해제한다. */
    @AfterEach
    void tearDown() {
        authUtils.close();
    }

    /** 검색값을 정규화하고 집계 Projection을 경량 후보 DTO로 변환한다. */
    @Test
    void getCandidatesNormalizesFiltersAndMapsMembershipState() throws Exception {
        final JournalThreadCandidateProjection projection = mock(JournalThreadCandidateProjection.class);
        when(projection.getId()).thenReturn(10);
        when(projection.getTitle()).thenReturn("가상 흐름");
        when(projection.getPrefixId()).thenReturn(101);
        when(projection.getPrefixName()).thenReturn("가상 말머리");
        when(projection.getPrefixActiveYn()).thenReturn("Y");
        when(projection.getLifecycleKey()).thenReturn("OPEN");
        when(projection.getMembershipCount()).thenReturn(3L);
        when(projection.getLastMembershipAt()).thenReturn(LocalDateTime.of(2026, 7, 20, 12, 0));
        when(projection.getCurrentEntryMembershipCount()).thenReturn(1L);
        when(repository.findCandidates(
                eq(FIXTURE_USERNAME),
                eq(FIXTURE_ENTRY_ID),
                eq("검색어"),
                eq(101),
                eq("N"),
                any(Pageable.class)
        )).thenReturn(List.of(projection));
        final List<JournalThreadCandidateDto> result =
                service.getCandidates(FIXTURE_ENTRY_ID, "  검색어  ", 101, false, 7);

        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getId());
        assertEquals(3L, result.get(0).getMembershipCount());
        assertEquals("OPEN", result.get(0).getLifecycleKey());
        assertEquals("가상 말머리", result.get(0).getPrefix().getName());
        assertTrue(result.get(0).isMember());
    }

    /** 요청 후보 수가 서버 상한을 넘으면 20개로 제한한다. */
    @Test
    void getCandidatesClampsLimitToServerMaximum() throws Exception {
        when(repository.findCandidates(
                eq(FIXTURE_USERNAME),
                eq(FIXTURE_ENTRY_ID),
                eq(""),
                isNull(),
                eq("Y"),
                any(Pageable.class)
        )).thenReturn(List.of());

        service.getCandidates(FIXTURE_ENTRY_ID, null, null, true, 100);

        final ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findCandidates(
                eq(FIXTURE_USERNAME),
                eq(FIXTURE_ENTRY_ID),
                eq(""),
                isNull(),
                eq("Y"),
                pageableCaptor.capture()
        );
        assertEquals(20, pageableCaptor.getValue().getPageSize());
    }
}
