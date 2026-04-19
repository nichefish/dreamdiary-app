package io.nicheblog.dreamdiary.feature.attachable.state.policy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.state.StateKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AttachableContentStatePolicyTest
 */
class AttachableContentStatePolicyTest {

    @Test
    void chapterOnlyCollapsed() {
        assertTrue(AttachableContentStatePolicy.isAllowed(ContentType.JOURNAL_CHAPTER, StateKey.COLLAPSED));
        assertFalse(AttachableContentStatePolicy.isAllowed(ContentType.JOURNAL_CHAPTER, StateKey.RESOLVED));
    }

    @Test
    void diaryAllowsRefrnc() {
        assertTrue(AttachableContentStatePolicy.isAllowed(ContentType.JOURNAL_DIARY, StateKey.REFRNC));
    }

    @Test
    void dreamAllowsNhtmrAndHalluc() {
        assertTrue(AttachableContentStatePolicy.isAllowed(ContentType.JOURNAL_DREAM, StateKey.NHTMR));
        assertTrue(AttachableContentStatePolicy.isAllowed(ContentType.JOURNAL_DREAM, StateKey.HALLUC));
        assertFalse(AttachableContentStatePolicy.isAllowed(ContentType.JOURNAL_DIARY, StateKey.NHTMR));
    }

    @Test
    void interpretationExcludesRefrnc() {
        assertTrue(AttachableContentStatePolicy.isAllowed(ContentType.JOURNAL_INTERPRETATION, StateKey.RESOLVED));
        assertFalse(AttachableContentStatePolicy.isAllowed(ContentType.JOURNAL_INTERPRETATION, StateKey.REFRNC));
    }

    @Test
    void unregisteredContentTypeRejected() {
        assertFalse(AttachableContentStatePolicy.isAllowed(ContentType.BOARD, StateKey.COLLAPSED));
    }
}
