package io.nicheblog.dreamdiary.feature.attachable.state.policy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.state.StateKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AttachableContentStatePolicyTest
 * <pre>
 *  {@link StateKey} 는 접기·중요·참조 등 UI 토글용 상태이며, 해석 완료({@code RESOLVED}) 는 {@link io.nicheblog.dreamdiary.feature.attachable.lifecycle.LifecycleKey} 에서 다룬다.
 *  변경 전: 테스트가 존재하지 않는 {@code StateKey.RESOLVED} 를 참조해 compileTestJava 가 실패했다.
 *  변경 후: 실제 정책(챕터는 COLLAPSED 만)에 맞는 키로 부정 케이스를 검증한다.
 * </pre>
 */
class AttachableContentStatePolicyTest {

    @Test
    void chapterOnlyCollapsed() {
        assertTrue(AttachableContentStatePolicy.isAllowed(ContentType.JOURNAL_CHAPTER, StateKey.COLLAPSED));
        assertFalse(AttachableContentStatePolicy.isAllowed(ContentType.JOURNAL_CHAPTER, StateKey.IMPRTC));
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
    void reflectionAllowsCollapsedImprtcRefrnc() {
        assertTrue(AttachableContentStatePolicy.isAllowed(ContentType.JOURNAL_REFLECTION, StateKey.COLLAPSED));
        assertTrue(AttachableContentStatePolicy.isAllowed(ContentType.JOURNAL_REFLECTION, StateKey.IMPRTC));
        assertTrue(AttachableContentStatePolicy.isAllowed(ContentType.JOURNAL_REFLECTION, StateKey.REFRNC));
        assertFalse(AttachableContentStatePolicy.isAllowed(ContentType.JOURNAL_REFLECTION, StateKey.NHTMR));
    }

    @Test
    void unregisteredContentTypeRejected() {
        assertFalse(AttachableContentStatePolicy.isAllowed(ContentType.BOARD, StateKey.COLLAPSED));
    }
}
