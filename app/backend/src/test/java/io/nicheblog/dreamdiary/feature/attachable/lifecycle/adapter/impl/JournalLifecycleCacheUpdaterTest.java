package io.nicheblog.dreamdiary.feature.attachable.lifecycle.adapter.impl;

import io.nicheblog.dreamdiary.feature.attachable.lifecycle.LifecycleKey;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 저널 라이프사이클 보조 맵의 기본 상태 저장 계약 테스트.
 */
class JournalLifecycleCacheUpdaterTest {

    private static final Integer FIXTURE_CONTENT_ID = 101;

    /** OPEN은 맵 항목 부재로 표현한다. */
    @Test
    void applyCurrentKeyRemovesOpenEntry() {
        final Map<Integer, String> lifecycleMap = new HashMap<>();
        lifecycleMap.put(FIXTURE_CONTENT_ID, LifecycleKey.PENDING.key);

        JournalLifecycleCacheUpdater.applyCurrentKey(
                lifecycleMap,
                FIXTURE_CONTENT_ID,
                LifecycleKey.OPEN
        );

        assertFalse(lifecycleMap.containsKey(FIXTURE_CONTENT_ID));
    }

    /** 명시적 상태는 맵에 저장한다. */
    @Test
    void applyCurrentKeyStoresExplicitLifecycle() {
        final Map<Integer, String> lifecycleMap = new HashMap<>();

        JournalLifecycleCacheUpdater.applyCurrentKey(
                lifecycleMap,
                FIXTURE_CONTENT_ID,
                LifecycleKey.RESOLVED
        );

        assertEquals(LifecycleKey.RESOLVED.key, lifecycleMap.get(FIXTURE_CONTENT_ID));
    }
}
