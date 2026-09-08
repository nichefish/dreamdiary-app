package io.nicheblog.dreamdiary.feature.journal.entry.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.TagContentEntity;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.repository.jpa.TagContentRepository;
import io.nicheblog.dreamdiary.feature.journal._shared.security.JournalContentOwnershipGuard;
import io.nicheblog.dreamdiary.feature.journal.day.service.helper.JournalDayResolvedGuard;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryBulkTagReqDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryBulkTagResDto;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.service.my.JournalEntryMyTagService;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.service.JournalEntryEntityQueueService;
import io.nicheblog.dreamdiary.feature.journal.embedding.service.JournalEntryEmbeddingQueueService;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.global.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 저널 엔트리 일괄 태그 서비스 계약 검증.
 * <p>
 * 멱등 분기(신규 저장·소프트 삭제 복원·활성 no-op), 제거 분기, 쓰기 전 전량 검증
 * 실패 시 어떤 연결도 변경하지 않음을 고정한다. 모든 픽스처는 가상값이다.
 */
@ExtendWith(MockitoExtension.class)
class JournalEntryBulkTagServiceTest {

    private static final String FIXTURE_USERNAME = "fixture_user";
    private static final String FIXTURE_CONTENT_TYPE = "JOURNAL_DREAM";
    private static final ContentType FIXTURE_CT = ContentType.JOURNAL_DREAM;
    private static final int FIXTURE_ENTRY_ID = 101;
    private static final int FIXTURE_TAG_ID = 11;
    private static final int FIXTURE_ROW_ID = 999;

    @Mock
    private JournalEntryRepository journalEntryRepository;
    @Mock
    private TagContentRepository tagContentRepository;
    @Mock
    private JournalContentOwnershipGuard ownershipGuard;
    @Mock
    private JournalDayResolvedGuard resolvedGuard;
    @Mock
    private JournalEntryMyTagService journalEntryMyTagService;
    @Mock
    private JournalEntryEntityQueueService journalEntryEntityQueueService;
    @Mock
    private JournalEntryEmbeddingQueueService journalEntryEmbeddingQueueService;

    @InjectMocks
    private JournalEntryBulkTagService service;

    private MockedStatic<AuthUtils> authUtils;
    private MockedStatic<EhCacheUtils> cacheUtils;

    @BeforeEach
    void setUp() {
        authUtils = mockStatic(AuthUtils.class);
        cacheUtils = mockStatic(EhCacheUtils.class);
        authUtils.when(AuthUtils::requireLoginUsername).thenReturn(FIXTURE_USERNAME);
    }

    @AfterEach
    void tearDown() {
        cacheUtils.close();
        authUtils.close();
    }

    /** 검증 통과용 공통 스텁 — 엔트리 존재·타입 일치, 태그 사용자 소속. */
    private void stubValidationPass() throws Exception {
        lenient().when(journalEntryRepository.findByIdAndContentType(FIXTURE_ENTRY_ID, FIXTURE_CONTENT_TYPE))
                .thenReturn(Optional.of(mock(JournalEntryEntity.class)));
        lenient().when(journalEntryMyTagService.getMyTagList(FIXTURE_CT))
                .thenReturn(List.of(new TagDto(FIXTURE_TAG_ID, "가상태그", "가상카테고리")));
    }

    private JournalEntryBulkTagReqDto req(final String operation) {
        return JournalEntryBulkTagReqDto.builder()
                .operation(operation)
                .contentType(FIXTURE_CONTENT_TYPE)
                .entryIds(List.of(FIXTURE_ENTRY_ID))
                .tagIds(List.of(FIXTURE_TAG_ID))
                .build();
    }

    /** ADD: 기존 연결이 없으면 새로 저장하고 변경으로 집계한다. */
    @Test
    void addInsertsNewLink() throws Exception {
        stubValidationPass();
        when(tagContentRepository.findAnyByPair(FIXTURE_TAG_ID, FIXTURE_ENTRY_ID, FIXTURE_CONTENT_TYPE, FIXTURE_USERNAME))
                .thenReturn(Optional.empty());

        final JournalEntryBulkTagResDto res = service.bulkTag(req("ADD"));

        verify(tagContentRepository).save(any(TagContentEntity.class));
        assertEquals(1, res.getChangedLinkCount());
        assertEquals(0, res.getUnchangedLinkCount());
        assertEquals(1, res.getAffectedEntryCount());
    }

    /** ADD: 소프트 삭제된 연결은 복원하고 새로 저장하지 않는다. */
    @Test
    void addRevivesSoftDeleted() throws Exception {
        stubValidationPass();
        final TagContentEntity soft = mock(TagContentEntity.class);
        when(soft.getDeletedAt()).thenReturn(LocalDateTime.now());
        when(soft.getId()).thenReturn(FIXTURE_ROW_ID);
        when(tagContentRepository.findAnyByPair(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(Optional.of(soft));

        final JournalEntryBulkTagResDto res = service.bulkTag(req("ADD"));

        verify(tagContentRepository).reviveById(FIXTURE_ROW_ID);
        verify(tagContentRepository, never()).save(any());
        assertEquals(1, res.getChangedLinkCount());
    }

    /** ADD: 이미 활성인 연결은 변경 없이 no-op으로 집계한다. */
    @Test
    void addActiveIsNoOp() throws Exception {
        stubValidationPass();
        final TagContentEntity active = mock(TagContentEntity.class);
        when(active.getDeletedAt()).thenReturn(null);
        when(tagContentRepository.findAnyByPair(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(Optional.of(active));

        final JournalEntryBulkTagResDto res = service.bulkTag(req("ADD"));

        verify(tagContentRepository, never()).save(any());
        verify(tagContentRepository, never()).reviveById(any());
        assertEquals(0, res.getChangedLinkCount());
        assertEquals(1, res.getUnchangedLinkCount());
        assertEquals(0, res.getAffectedEntryCount());
    }

    /** REMOVE: 활성 연결을 소프트 삭제하고 변경으로 집계한다. */
    @Test
    void removeSoftDeletesActive() throws Exception {
        stubValidationPass();
        final TagContentEntity active = mock(TagContentEntity.class);
        when(active.getDeletedAt()).thenReturn(null);
        when(tagContentRepository.findAnyByPair(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(Optional.of(active));

        final JournalEntryBulkTagResDto res = service.bulkTag(req("REMOVE"));

        verify(tagContentRepository).delete(active);
        assertEquals(1, res.getChangedLinkCount());
    }

    /** REMOVE: 연결이 없으면 no-op으로 집계한다. */
    @Test
    void removeMissingIsNoOp() throws Exception {
        stubValidationPass();
        when(tagContentRepository.findAnyByPair(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        final JournalEntryBulkTagResDto res = service.bulkTag(req("REMOVE"));

        verify(tagContentRepository, never()).delete(any());
        assertEquals(1, res.getUnchangedLinkCount());
    }

    /** 빈 선택은 쓰기 전에 거부한다. */
    @Test
    void emptySelectionRejected() {
        final JournalEntryBulkTagReqDto req = JournalEntryBulkTagReqDto.builder()
                .operation("ADD").contentType(FIXTURE_CONTENT_TYPE)
                .entryIds(List.of()).tagIds(List.of(FIXTURE_TAG_ID)).build();

        assertThrows(BusinessException.class, () -> service.bulkTag(req));
        verify(tagContentRepository, never()).save(any());
    }

    /** 존재하지 않는 엔트리는 어떤 연결도 쓰기 전에 거부한다. */
    @Test
    void entryNotFoundRejectedBeforeWrite() throws Exception {
        when(journalEntryRepository.findByIdAndContentType(FIXTURE_ENTRY_ID, FIXTURE_CONTENT_TYPE))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.bulkTag(req("ADD")));
        verify(tagContentRepository, never()).save(any());
        verify(tagContentRepository, never()).delete(any());
    }

    /** 사용자 태그 목록에 없는 태그는 쓰기 전에 거부한다. */
    @Test
    void tagNotOwnedRejected() throws Exception {
        when(journalEntryRepository.findByIdAndContentType(FIXTURE_ENTRY_ID, FIXTURE_CONTENT_TYPE))
                .thenReturn(Optional.of(mock(JournalEntryEntity.class)));
        when(journalEntryMyTagService.getMyTagList(FIXTURE_CT)).thenReturn(List.of());

        assertThrows(BusinessException.class, () -> service.bulkTag(req("ADD")));
        verify(tagContentRepository, never()).save(any());
    }

    /** 지원하지 않는 작업 종류는 거부한다. */
    @Test
    void invalidOperationRejected() {
        assertThrows(BusinessException.class, () -> service.bulkTag(req("REPLACE")));
    }
}