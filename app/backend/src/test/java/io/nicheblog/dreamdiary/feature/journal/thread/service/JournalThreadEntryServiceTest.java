package io.nicheblog.dreamdiary.feature.journal.thread.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.service.JournalEntryService;
import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadEntity;
import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadEntryDto;
import io.nicheblog.dreamdiary.feature.journal.thread.repository.jpa.JournalThreadEntryRepository;
import io.nicheblog.dreamdiary.feature.journal.thread.repository.jpa.JournalThreadRepository;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 저널 스레드-엔트리 소속 서비스 계약 검증.
 * <p>
 * 대상 엔트리 소유권 검증, 신규 등록, 멱등 처리, 소프트 삭제 복원,
 * N:M 조회와 상세 표시 순서를 고정한다.
 */
@ExtendWith(MockitoExtension.class)
class JournalThreadEntryServiceTest {

    private static final String FIXTURE_USERNAME = "fixture_user";
    private static final String FIXTURE_OWNER = "fixture_owner";
    private static final int FIXTURE_THREAD_ID = 10;
    private static final int FIXTURE_ENTRY_ID = 20;
    private static final int FIXTURE_MEMBERSHIP_ID = 30;

    @Mock
    private JournalThreadEntryRepository repository;
    @Mock
    private JournalThreadRepository journalThreadRepository;
    @Mock
    private JournalEntryService journalEntryService;

    @InjectMocks
    private JournalThreadEntryService service;

    private MockedStatic<AuthUtils> authUtils;

    /** 인증 사용자와 본인 소유 스레드를 공통 픽스처로 준비한다. */
    @BeforeEach
    void setUp() throws Exception {
        authUtils = mockStatic(AuthUtils.class);
        authUtils.when(AuthUtils::requireLoginUsername).thenReturn(FIXTURE_USERNAME);
        authUtils.when(() -> AuthUtils.isCreatedBy(FIXTURE_OWNER)).thenReturn(true);
        lenient().when(journalThreadRepository.findById(FIXTURE_THREAD_ID)).thenReturn(Optional.of(ownedThread()));
        lenient().when(journalEntryService.getDtlEntity(FIXTURE_ENTRY_ID)).thenReturn(ownedEntry());
    }

    /** 정적 인증 mock을 테스트마다 해제한다. */
    @AfterEach
    void tearDown() {
        authUtils.close();
    }

    /** 기존 소속이 없으면 새 행을 저장하고 성공한다. */
    @Test
    void registCreatesNewMembership() throws Exception {
        when(repository.findAnyByPair(FIXTURE_THREAD_ID, FIXTURE_ENTRY_ID, FIXTURE_USERNAME))
                .thenReturn(Optional.empty());
        when(repository.save(any(JournalThreadEntryEntity.class))).thenAnswer(invocation -> {
            final JournalThreadEntryEntity entity = invocation.getArgument(0);
            entity.setId(FIXTURE_MEMBERSHIP_ID);
            entity.setCreatedBy(FIXTURE_USERNAME);
            return entity;
        });

        final ServiceResponse response = service.regist(FIXTURE_THREAD_ID, FIXTURE_ENTRY_ID, null);

        assertTrue(response.getRslt());
        final ArgumentCaptor<JournalThreadEntryEntity> captor = ArgumentCaptor.forClass(JournalThreadEntryEntity.class);
        verify(repository).save(captor.capture());
        assertEquals(FIXTURE_THREAD_ID, captor.getValue().getThreadId());
        assertEquals(FIXTURE_ENTRY_ID, captor.getValue().getEntryId());
        verify(journalEntryService).getDtlEntity(FIXTURE_ENTRY_ID);
    }

    /** 살아 있는 소속을 다시 등록해도 INSERT나 복원을 수행하지 않는다. */
    @Test
    void registIsIdempotentForActiveMembership() throws Exception {
        when(repository.findAnyByPair(FIXTURE_THREAD_ID, FIXTURE_ENTRY_ID, FIXTURE_USERNAME))
                .thenReturn(Optional.of(activeMembership(FIXTURE_THREAD_ID, FIXTURE_ENTRY_ID)));

        final ServiceResponse response = service.regist(FIXTURE_THREAD_ID, FIXTURE_ENTRY_ID, null);

        assertTrue(response.getRslt());
        verify(repository, never()).save(any());
        verify(repository, never()).reviveById(any());
    }

    /** 소프트 삭제된 소속을 다시 등록하면 새 행 대신 기존 행을 복원한다. */
    @Test
    void registRevivesSoftDeletedMembership() throws Exception {
        final JournalThreadEntryEntity deleted = activeMembership(FIXTURE_THREAD_ID, FIXTURE_ENTRY_ID);
        deleted.setDeletedAt(LocalDateTime.of(2026, 7, 1, 0, 0));
        when(repository.findAnyByPair(FIXTURE_THREAD_ID, FIXTURE_ENTRY_ID, FIXTURE_USERNAME))
                .thenReturn(Optional.of(deleted));

        final ServiceResponse response = service.regist(FIXTURE_THREAD_ID, FIXTURE_ENTRY_ID, null);

        assertTrue(response.getRslt());
        verify(repository).reviveById(deleted.getId());
        verify(repository, never()).save(any());
    }

    /** 살아 있는 소속은 repository delete를 통해 소프트 삭제한다. */
    @Test
    void deleteRemovesActiveMembership() throws Exception {
        final JournalThreadEntryEntity active = activeMembership(FIXTURE_THREAD_ID, FIXTURE_ENTRY_ID);
        when(repository.findAnyByPair(FIXTURE_THREAD_ID, FIXTURE_ENTRY_ID, FIXTURE_USERNAME))
                .thenReturn(Optional.of(active));

        final ServiceResponse response = service.delete(FIXTURE_THREAD_ID, FIXTURE_ENTRY_ID);

        assertTrue(response.getRslt());
        verify(repository).delete(active);
    }

    /** 이미 없는 소속을 해제해도 성공하며 추가 삭제를 수행하지 않는다. */
    @Test
    void deleteIsIdempotentWhenMembershipIsMissing() throws Exception {
        when(repository.findAnyByPair(FIXTURE_THREAD_ID, FIXTURE_ENTRY_ID, FIXTURE_USERNAME))
                .thenReturn(Optional.empty());

        final ServiceResponse response = service.delete(FIXTURE_THREAD_ID, FIXTURE_ENTRY_ID);

        assertTrue(response.getRslt());
        verify(repository, never()).delete(any());
    }

    /** 한 엔트리가 여러 스레드에 속한 소속 목록을 모두 반환한다. */
    @Test
    void getListByEntryReturnsMultipleThreadMemberships() throws Exception {
        when(repository.findAllByEntryIdAndCreatedByOrderByCreatedAtAsc(FIXTURE_ENTRY_ID, FIXTURE_USERNAME))
                .thenReturn(List.of(
                        activeMembership(FIXTURE_THREAD_ID, FIXTURE_ENTRY_ID),
                        activeMembership(FIXTURE_THREAD_ID + 1, FIXTURE_ENTRY_ID)
                ));

        final List<JournalThreadEntryDto> result = service.getListByEntry(FIXTURE_ENTRY_ID);

        assertEquals(List.of(FIXTURE_THREAD_ID, FIXTURE_THREAD_ID + 1),
                result.stream().map(JournalThreadEntryDto::getThreadId).toList());
    }

    /** 상세 엔트리는 일자 오름차순, 동일 일자는 ID 오름차순으로 고정한다. */
    @Test
    void getEntriesByThreadUsesDeterministicDateAndIdOrder() throws Exception {
        when(repository.findAllByThread(FIXTURE_THREAD_ID, FIXTURE_USERNAME)).thenReturn(List.of(
                activeMembership(FIXTURE_THREAD_ID, 3),
                activeMembership(FIXTURE_THREAD_ID, 2),
                activeMembership(FIXTURE_THREAD_ID, 1)
        ));
        when(journalEntryService.getListDtoByIds(List.of(3, 2, 1))).thenReturn(new ArrayList<>(List.of(
                entry(3, "2026-07-03"),
                entry(2, "2026-07-01"),
                entry(1, "2026-07-03")
        )));

        final List<JournalEntryDto> result = service.getEntriesByThread(FIXTURE_THREAD_ID);

        assertEquals(List.of(2, 1, 3), result.stream().map(JournalEntryDto::getId).toList());
    }

    /** 타인 소유 스레드에는 소속을 등록할 수 없다. */
    @Test
    void registRejectsThreadNotOwnedByCurrentUser() {
        authUtils.when(() -> AuthUtils.isCreatedBy(FIXTURE_OWNER)).thenReturn(false);

        assertThrows(NotAuthorizedException.class,
                () -> service.regist(FIXTURE_THREAD_ID, FIXTURE_ENTRY_ID, null));
        verify(repository, never()).save(any());
    }

    /** 존재하지 않는 엔트리는 소속 행 조회·저장·복원 전에 거부한다. */
    @Test
    void registRejectsMissingEntryBeforeMembershipWrite() throws Exception {
        when(journalEntryService.getDtlEntity(FIXTURE_ENTRY_ID)).thenThrow(new EntityNotFoundException());

        assertThrows(EntityNotFoundException.class,
                () -> service.regist(FIXTURE_THREAD_ID, FIXTURE_ENTRY_ID, null));

        verify(repository, never()).findAnyByPair(any(), any(), any());
        verify(repository, never()).save(any());
        verify(repository, never()).reviveById(any());
    }

    /** 타인 소유 엔트리는 소속 행 조회·저장·복원 전에 거부한다. */
    @Test
    void registRejectsEntryNotOwnedByCurrentUserBeforeMembershipWrite() throws Exception {
        when(journalEntryService.getDtlEntity(FIXTURE_ENTRY_ID)).thenReturn(
                JournalEntryEntity.builder()
                        .id(FIXTURE_ENTRY_ID)
                        .createdBy("fixture_other_user")
                        .build()
        );

        assertThrows(NotAuthorizedException.class,
                () -> service.regist(FIXTURE_THREAD_ID, FIXTURE_ENTRY_ID, null));

        verify(repository, never()).findAnyByPair(any(), any(), any());
        verify(repository, never()).save(any());
        verify(repository, never()).reviveById(any());
    }

    private JournalThreadEntity ownedThread() {
        return JournalThreadEntity.builder()
                .id(FIXTURE_THREAD_ID)
                .title("가상 흐름")
                .createdBy(FIXTURE_OWNER)
                .build();
    }

    private JournalThreadEntryEntity activeMembership(final int threadId, final int entryId) {
        return JournalThreadEntryEntity.builder()
                .id(FIXTURE_MEMBERSHIP_ID + threadId + entryId)
                .threadId(threadId)
                .entryId(entryId)
                .createdBy(FIXTURE_USERNAME)
                .build();
    }

    private JournalEntryEntity ownedEntry() {
        return JournalEntryEntity.builder()
                .id(FIXTURE_ENTRY_ID)
                .createdBy(FIXTURE_USERNAME)
                .build();
    }

    private JournalEntryDto entry(final int id, final String stdrdDt) {
        return JournalEntryDto.builder()
                .id(id)
                .stdrdDt(stdrdDt)
                .build();
    }
}
