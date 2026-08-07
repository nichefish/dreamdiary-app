package io.nicheblog.dreamdiary.feature.journal._shared.security;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterSmpEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import io.nicheblog.dreamdiary.feature.journal.reflection.repository.jpa.JournalReflectionRepository;
import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadEntity;
import io.nicheblog.dreamdiary.feature.journal.thread.repository.jpa.JournalThreadRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 저널 부착 상태·라이프사이클 원본 콘텐츠 소유권 가드 테스트.
 *
 * @author nichefish
 */
@ExtendWith(MockitoExtension.class)
class JournalContentOwnershipGuardTest {

    private static final String FIXTURE_OWNER = "fixture_journal_owner";
    private static final String FIXTURE_OTHER_OWNER = "fixture_other_owner";
    private static final Integer FIXTURE_DAY_ID = 100;
    private static final Integer FIXTURE_CHAPTER_ID = 101;
    private static final Integer FIXTURE_ENTRY_ID = 102;
    private static final Integer FIXTURE_THREAD_ID = 104;

    @Mock
    private JournalDayRepository journalDayRepository;
    @Mock
    private JournalChapterRepository journalChapterRepository;
    @Mock
    private JournalEntryRepository journalEntryRepository;
    @Mock
    private JournalReflectionRepository journalReflectionRepository;
    @Mock
    private JournalThreadRepository journalThreadRepository;

    private MockedStatic<AuthUtils> authUtils;
    private JournalContentOwnershipGuard guard;

    @BeforeEach
    void setUp() {
        authUtils = mockStatic(AuthUtils.class);
        authUtils.when(AuthUtils::requireLoginUsername).thenReturn(FIXTURE_OWNER);
        guard = new JournalContentOwnershipGuard(
                journalDayRepository,
                journalChapterRepository,
                journalEntryRepository,
                journalReflectionRepository,
                journalThreadRepository
        );
    }

    @AfterEach
    void tearDown() {
        authUtils.close();
    }

    /** 일자와 챕터는 각 원본 행의 작성자 소유권을 사용한다. */
    @Test
    void ownedDayAndChapterAreAccepted() {
        when(journalDayRepository.findById(FIXTURE_DAY_ID)).thenReturn(Optional.of(
                JournalDayEntity.builder().id(FIXTURE_DAY_ID).createdBy(FIXTURE_OWNER).build()
        ));
        when(journalChapterRepository.findById(FIXTURE_CHAPTER_ID)).thenReturn(Optional.of(
                JournalChapterEntity.builder().id(FIXTURE_CHAPTER_ID).createdBy(FIXTURE_OWNER).build()
        ));

        assertDoesNotThrow(() -> guard.assertOwned(FIXTURE_DAY_ID, ContentType.JOURNAL_DAY));
        assertDoesNotThrow(() -> guard.assertOwned(FIXTURE_CHAPTER_ID, ContentType.JOURNAL_CHAPTER));
    }

    /** 일기·노트·꿈은 같은 원본 엔트리의 작성자 소유권을 사용한다. */
    @ParameterizedTest
    @EnumSource(value = ContentType.class, names = {
            "JOURNAL_DIARY",
            "JOURNAL_NOTE",
            "JOURNAL_DREAM"
    })
    void ownedEntryTypesAreAccepted(final ContentType contentType) {
        when(journalEntryRepository.findById(FIXTURE_ENTRY_ID)).thenReturn(Optional.of(
                JournalEntryEntity.builder()
                        .id(FIXTURE_ENTRY_ID)
                        .createdBy(FIXTURE_OWNER)
                        .journalChapter(chapter(contentType))
                        .build()
        ));

        assertDoesNotThrow(() -> guard.assertOwned(FIXTURE_ENTRY_ID, contentType));
    }

    /** 스레드는 원본 작성자가 현재 사용자이면 허용한다. */
    @Test
    void ownedThreadIsAccepted() {
        when(journalThreadRepository.findById(FIXTURE_THREAD_ID)).thenReturn(Optional.of(
                JournalThreadEntity.builder()
                        .id(FIXTURE_THREAD_ID)
                        .createdBy(FIXTURE_OWNER)
                        .build()
        ));

        assertDoesNotThrow(() -> guard.assertOwned(FIXTURE_THREAD_ID, ContentType.JOURNAL_THREAD));
    }

    /** 다른 사용자가 작성한 모든 지원 대상은 권한 오류로 거부한다. */
    @Test
    void contentOwnedByAnotherUserIsRejectedForAllDomains() {
        when(journalDayRepository.findById(FIXTURE_DAY_ID)).thenReturn(Optional.of(
                JournalDayEntity.builder().id(FIXTURE_DAY_ID).createdBy(FIXTURE_OTHER_OWNER).build()
        ));
        when(journalChapterRepository.findById(FIXTURE_CHAPTER_ID)).thenReturn(Optional.of(
                JournalChapterEntity.builder().id(FIXTURE_CHAPTER_ID).createdBy(FIXTURE_OTHER_OWNER).build()
        ));
        when(journalEntryRepository.findById(FIXTURE_ENTRY_ID)).thenReturn(Optional.of(
                JournalEntryEntity.builder()
                        .id(FIXTURE_ENTRY_ID)
                        .createdBy(FIXTURE_OTHER_OWNER)
                        .journalChapter(chapter(ContentType.JOURNAL_DIARY))
                        .build()
        ));
        when(journalThreadRepository.findById(FIXTURE_THREAD_ID)).thenReturn(Optional.of(
                JournalThreadEntity.builder()
                        .id(FIXTURE_THREAD_ID)
                        .createdBy(FIXTURE_OTHER_OWNER)
                        .build()
        ));

        assertThrows(NotAuthorizedException.class,
                () -> guard.assertOwned(FIXTURE_DAY_ID, ContentType.JOURNAL_DAY));
        assertThrows(NotAuthorizedException.class,
                () -> guard.assertOwned(FIXTURE_CHAPTER_ID, ContentType.JOURNAL_CHAPTER));
        assertThrows(NotAuthorizedException.class,
                () -> guard.assertOwned(FIXTURE_ENTRY_ID, ContentType.JOURNAL_DIARY));
        assertThrows(NotAuthorizedException.class,
                () -> guard.assertOwned(FIXTURE_THREAD_ID, ContentType.JOURNAL_THREAD));
    }

    /** 존재하지 않는 원본 ID는 orphan 상태·라이프사이클을 만들지 못하도록 권한 오류로 거부한다. */
    @Test
    void missingContentIsRejected() {
        when(journalThreadRepository.findById(FIXTURE_THREAD_ID)).thenReturn(Optional.empty());

        assertThrows(NotAuthorizedException.class,
                () -> guard.assertOwned(FIXTURE_THREAD_ID, ContentType.JOURNAL_THREAD));
    }

    /** 자기 엔트리라도 요청 콘텐츠 타입과 실제 챕터 유형이 다르면 거부한다. */
    @Test
    void mismatchedEntryContentTypeIsRejected() {
        when(journalEntryRepository.findById(FIXTURE_ENTRY_ID)).thenReturn(Optional.of(
                JournalEntryEntity.builder()
                        .id(FIXTURE_ENTRY_ID)
                        .createdBy(FIXTURE_OWNER)
                        .journalChapter(chapter(ContentType.JOURNAL_DIARY))
                        .build()
        ));

        assertThrows(NotAuthorizedException.class,
                () -> guard.assertOwned(FIXTURE_ENTRY_ID, ContentType.JOURNAL_DREAM));
    }

    /** 정책 밖 콘텐츠 타입은 원본 저장소를 조회하지 않고 fail-closed로 거부한다. */
    @Test
    void unsupportedContentTypeIsRejectedWithoutRepositoryLookup() {
        assertThrows(NotAuthorizedException.class,
                () -> guard.assertOwned(201, ContentType.BOARD));

        verifyNoInteractions(
                journalDayRepository,
                journalChapterRepository,
                journalEntryRepository,
                journalThreadRepository
        );
    }

    private JournalChapterSmpEntity chapter(final ContentType contentType) {
        final ChapterType chapterType = switch (contentType) {
            case JOURNAL_DIARY -> ChapterType.DIARY;
            case JOURNAL_NOTE -> ChapterType.NOTE;
            case JOURNAL_DREAM -> ChapterType.DREAM;
            default -> throw new IllegalArgumentException("Unsupported entry content type: " + contentType);
        };
        return JournalChapterSmpEntity.builder()
                .chapterType(chapterType)
                .build();
    }
}
