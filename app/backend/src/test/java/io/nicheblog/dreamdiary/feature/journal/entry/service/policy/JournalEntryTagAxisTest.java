package io.nicheblog.dreamdiary.feature.journal.entry.service.policy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 저널 엔트리 태그의 집계·검색 축 계약을 검증한다.
 * DIARY/DREAM은 요청 타입 단독 축을 사용하고 Reflection은 태그 축을 갖지 않는 현재 구조를 고정한다.
 */
class JournalEntryTagAxisTest {

    @Test
    @DisplayName("DIARY 태그 집계 축은 DIARY 단독이다")
    void expandKeys_diaryAlone() {
        final List<String> keys = JournalEntryTagAxis.expandKeys(ContentType.JOURNAL_DIARY);

        assertEquals(List.of(ContentType.JOURNAL_DIARY.key), keys);
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
    @DisplayName("REFLECTION은 태그 집계·검색 축을 갖지 않는다")
    void reflectionHasNoTagScope() {
        assertTrue(JournalEntryTagAxis.expandKeys(ContentType.JOURNAL_REFLECTION).isEmpty());
        assertTrue(JournalEntryTagAxis.searchScopeKeys(ContentType.JOURNAL_REFLECTION).isEmpty());
    }
}
