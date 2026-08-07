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
    // canBeReflectionTarget()
    // =========================================================

    @Test
    void canBeReflectionTarget_trueForDiaryDreamReflection() {
        assertTrue(JournalEntryTypePolicy.DIARY.canBeReflectionTarget());
        assertTrue(JournalEntryTypePolicy.DREAM.canBeReflectionTarget());
        assertTrue(JournalEntryTypePolicy.REFLECTION.canBeReflectionTarget());
    }

    // =========================================================
    // reflectionTargetTypes()
    // =========================================================

    @Test
    void reflectionTargetTypes_containsDiaryDreamReflection() {
        final List<JournalEntryTypePolicy> types = JournalEntryTypePolicy.reflectionTargetTypes();

        assertEquals(3, types.size());
        assertTrue(types.contains(JournalEntryTypePolicy.DIARY));
        assertTrue(types.contains(JournalEntryTypePolicy.DREAM));
        assertTrue(types.contains(JournalEntryTypePolicy.REFLECTION));
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
    // isEntryType()
    // =========================================================

    @Test
    void isEntryType_trueForDiaryAndDream() {
        assertTrue(JournalEntryTypePolicy.isEntryType(ContentType.JOURNAL_DIARY));
        assertTrue(JournalEntryTypePolicy.isEntryType(ContentType.JOURNAL_DREAM));
    }

    @Test
    void isEntryType_falseForDefaultAndNull() {
        assertFalse(JournalEntryTypePolicy.isEntryType(ContentType.DEFAULT));
        assertFalse(JournalEntryTypePolicy.isEntryType(null));
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
