package io.nicheblog.dreamdiary.feature.journal.entry.service.policy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JournalEntryTypePolicyTest {

    // =========================================================
    // from()
    // =========================================================

    @Test
    void from_returnsCorrectPolicyForEachContentType() {
        assertEquals(JournalEntryTypePolicy.DIARY, JournalEntryTypePolicy.from(ContentType.JOURNAL_DIARY));
        assertEquals(JournalEntryTypePolicy.DREAM, JournalEntryTypePolicy.from(ContentType.JOURNAL_DREAM));
    }

    @Test
    void from_defaultsToDiaryForUnrelatedContentType() {
        assertEquals(JournalEntryTypePolicy.DIARY, JournalEntryTypePolicy.from(ContentType.DEFAULT));
    }

    // =========================================================
    // supportsInterpretation()
    // =========================================================

    @Test
    void supportsInterpretation_trueForDiaryAndDream() {
        assertTrue(JournalEntryTypePolicy.DIARY.supportsInterpretation());
        assertTrue(JournalEntryTypePolicy.DREAM.supportsInterpretation());
    }

    // =========================================================
    // interpretableTypes()
    // =========================================================

    @Test
    void interpretableTypes_containsOnlyDiaryAndDream() {
        final List<JournalEntryTypePolicy> types = JournalEntryTypePolicy.interpretableTypes();

        assertEquals(2, types.size());
        assertTrue(types.contains(JournalEntryTypePolicy.DIARY));
        assertTrue(types.contains(JournalEntryTypePolicy.DREAM));
    }

    // =========================================================
    // supportsChapterChange()
    // =========================================================

    @Test
    void supportsChapterChange_trueForDiary() {
        assertTrue(JournalEntryTypePolicy.DIARY.supportsChapterChange());
    }

    @Test
    void supportsChapterChange_falseForDream() {
        assertFalse(JournalEntryTypePolicy.DREAM.supportsChapterChange());
    }

    // =========================================================
    // reorderOnDelete()
    // =========================================================

    @Test
    void reorderOnDelete_trueOnlyForDream() {
        assertFalse(JournalEntryTypePolicy.DIARY.reorderOnDelete());
        assertTrue(JournalEntryTypePolicy.DREAM.reorderOnDelete());
    }

    // =========================================================
    // resolveModifiedChapterId()
    // =========================================================

    @Test
    void resolveModifiedChapterId_dreamFallsBackToEntityChapterWhenDtoIsNull() {
        final Integer resolved = JournalEntryTypePolicy.DREAM.resolveModifiedChapterId(null, 5);
        assertEquals(5, resolved);
    }

    @Test
    void resolveModifiedChapterId_dreamPrefersDtoChapterWhenPresent() {
        final Integer resolved = JournalEntryTypePolicy.DREAM.resolveModifiedChapterId(3, 5);
        assertEquals(3, resolved);
    }

    @Test
    void resolveModifiedChapterId_diaryPassesThroughDtoChapterId() {
        assertNull(JournalEntryTypePolicy.DIARY.resolveModifiedChapterId(null, 5));
        assertEquals(7, JournalEntryTypePolicy.DIARY.resolveModifiedChapterId(7, 5));
    }

    // =========================================================
    // stateCacheName()
    // =========================================================

    @Test
    void stateCacheName_notNullForDiaryAndDream() {
        assertNotNull(JournalEntryTypePolicy.DIARY.stateCacheName());
        assertNotNull(JournalEntryTypePolicy.DREAM.stateCacheName());
    }

    // =========================================================
    // expectedChapterType
    // =========================================================

    @Test
    void expectedChapterType_matchesContentType() {
        assertEquals(io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType.DIARY, JournalEntryTypePolicy.DIARY.expectedChapterType);
        assertEquals(io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType.DREAM, JournalEntryTypePolicy.DREAM.expectedChapterType);
    }
}
