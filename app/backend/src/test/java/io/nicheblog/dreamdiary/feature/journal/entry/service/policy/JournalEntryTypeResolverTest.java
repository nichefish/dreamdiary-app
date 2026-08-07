package io.nicheblog.dreamdiary.feature.journal.entry.service.policy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 등록 타입 결정 계약: Reflection은 요청 contentType을 존중하고, DIARY/DREAM은 챕터에서 역산한다.
 */
@ExtendWith(MockitoExtension.class)
class JournalEntryTypeResolverTest {

    @Mock
    private JournalChapterRepository journalChapterRepository;
    @Mock
    private JournalEntryRepository journalEntryRepository;
    @Mock
    private JournalEntryPolicyResolver policyResolver;
    @InjectMocks
    private JournalEntryTypeResolver typeResolver;

    /** 일기 챕터에 JOURNAL_REFLECTION을 요청하면 챕터 역산 없이 Reflection을 반환한다. */
    @Test
    void resolveForRegist_honorsRequestedReflection() {
        final ContentType resolved = typeResolver.resolveForRegist(10, ContentType.JOURNAL_REFLECTION.key);

        assertEquals(ContentType.JOURNAL_REFLECTION, resolved);
        verifyNoInteractions(journalChapterRepository);
    }

    /** Reflection이 아니면 일기 챕터에서 JOURNAL_DIARY로 역산한다. */
    @Test
    void resolveForRegist_derivesDiaryFromDiaryChapter() {
        when(journalChapterRepository.findById(10)).thenReturn(Optional.of(
                JournalChapterEntity.builder().id(10).chapterType(ChapterType.DIARY).build()
        ));

        final ContentType resolved = typeResolver.resolveForRegist(10, ContentType.JOURNAL_DIARY.key);

        assertEquals(ContentType.JOURNAL_DIARY, resolved);
    }

    /** contentType 누락·비Reflection이면 챕터 역산 경로를 탄다. */
    @Test
    void resolveForRegist_blankFallsBackToChapter() {
        when(journalChapterRepository.findById(10)).thenReturn(Optional.of(
                JournalChapterEntity.builder().id(10).chapterType(ChapterType.DIARY).build()
        ));

        final ContentType resolved = typeResolver.resolveForRegist(10, null);

        assertEquals(ContentType.JOURNAL_DIARY, resolved);
    }
}
