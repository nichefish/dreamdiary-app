package io.nicheblog.dreamdiary.feature.journal.entry.service.policy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JournalEntryTagAxisTest {

    @Test
    @DisplayName("DIARY tag query expands to DIARY + REFLECTION")
    void expandKeys_diaryIncludesReflection() {
        final List<String> keys = JournalEntryTagAxis.expandKeys(ContentType.JOURNAL_DIARY);

        assertEquals(
                List.of(ContentType.JOURNAL_DIARY.key, ContentType.JOURNAL_REFLECTION.key),
                keys
        );
    }

    @Test
    @DisplayName("DREAM tag query stays DREAM alone")
    void expandKeys_dreamAlone() {
        assertEquals(
                List.of(ContentType.JOURNAL_DREAM.key),
                JournalEntryTagAxis.expandKeys(ContentType.JOURNAL_DREAM)
        );
    }

    @Test
    @DisplayName("DIARY search scope stays DIARY alone (reflection excluded)")
    void searchScopeKeys_diaryExcludesReflection() {
        assertEquals(
                List.of(ContentType.JOURNAL_DIARY.key),
                JournalEntryTagAxis.searchScopeKeys(ContentType.JOURNAL_DIARY)
        );
    }

    @Test
    @DisplayName("DREAM search scope stays DREAM alone")
    void searchScopeKeys_dreamAlone() {
        assertEquals(
                List.of(ContentType.JOURNAL_DREAM.key),
                JournalEntryTagAxis.searchScopeKeys(ContentType.JOURNAL_DREAM)
        );
    }

    @Test
    @DisplayName("REFLECTION tag change evicts diary axis caches")
    void evictsDiaryAxis_reflectionOnly() {
        assertTrue(JournalEntryTagAxis.evictsDiaryAxis(ContentType.JOURNAL_REFLECTION));
        assertFalse(JournalEntryTagAxis.evictsDiaryAxis(ContentType.JOURNAL_DIARY));
    }
}
