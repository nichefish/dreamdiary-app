package io.nicheblog.dreamdiary.feature.chat.service;

import io.nicheblog.dreamdiary.feature.chat.model.RagIntent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RagIntentClassifier} 휴리스틱 분류 회귀 테스트.
 *
 * <p>가상 픽스처만 사용한다 ({@code 민수}/{@code 지연}).</p>
 */
class RagIntentClassifierTest {

    /** 가상 인물 A */
    private static final String FIXTURE_PERSON_A = "민수";

    @Test
    @DisplayName("빈 질문은 LOOKUP")
    void classify_blank_isLookup() {
        assertEquals(RagIntent.LOOKUP, RagIntentClassifier.classify("  ", false));
        assertEquals(RagIntent.LOOKUP, RagIntentClassifier.classify(null, false));
    }

    @Test
    @DisplayName("인물 about 플래그는 SYNTHESIS")
    void classify_personAbout_isSynthesis() {
        assertEquals(
                RagIntent.SYNTHESIS,
                RagIntentClassifier.classify(FIXTURE_PERSON_A + "에 대해 알려줘", true)
        );
    }

    @Test
    @DisplayName("태도 질문은 SYNTHESIS")
    void classify_attitude_isSynthesis() {
        assertEquals(
                RagIntent.SYNTHESIS,
                RagIntentClassifier.classify("나는 " + FIXTURE_PERSON_A + "님을 어떻게 생각하고 있니?", false)
        );
    }

    @Test
    @DisplayName("패턴·의미 질문은 SYNTHESIS")
    void classify_patternMeaning_isSynthesis() {
        assertEquals(RagIntent.SYNTHESIS, RagIntentClassifier.classify("내 기록의 반복 패턴을 해석해줘", false));
        assertEquals(RagIntent.SYNTHESIS, RagIntentClassifier.classify(FIXTURE_PERSON_A + "는 내 기록에서 어떤 의미야?", false));
    }

    @Test
    @DisplayName("명시적 요약 동사는 SUMMARY")
    void classify_summaryVerb_isSummary() {
        assertEquals(RagIntent.SUMMARY, RagIntentClassifier.classify("최근 꿈을 요약해줘", false));
        assertEquals(RagIntent.SUMMARY, RagIntentClassifier.classify("기록을 정리해줘", false));
    }

    @Test
    @DisplayName("최근 단독은 LOOKUP (오분류 완화)")
    void classify_recentAlone_isLookup() {
        assertEquals(RagIntent.LOOKUP, RagIntentClassifier.classify("최근 꿈", false));
        assertEquals(RagIntent.LOOKUP, RagIntentClassifier.classify("최근 기록", false));
    }

    @Test
    @DisplayName("최근+요약 동반은 SUMMARY")
    void classify_recentWithSummaryCompanion_isSummary() {
        assertEquals(RagIntent.SUMMARY, RagIntentClassifier.classify("최근 기록을 모아줘", false));
    }

    @Test
    @DisplayName("명시적 검색 단서는 LOOKUP 우선 (패턴/최근보다)")
    void classify_lookupForce_beatsSynthesisAndSummary() {
        assertEquals(RagIntent.LOOKUP, RagIntentClassifier.classify("최근 " + FIXTURE_PERSON_A + " 기록 찾아줘", false));
        assertEquals(RagIntent.LOOKUP, RagIntentClassifier.classify("패턴 관련 기록 검색해줘", false));
        assertEquals(RagIntent.LOOKUP, RagIntentClassifier.classify(FIXTURE_PERSON_A + " 태그가 어디에 있어?", false));
    }

    @Test
    @DisplayName("일반 질문은 LOOKUP")
    void classify_plain_isLookup() {
        assertEquals(RagIntent.LOOKUP, RagIntentClassifier.classify(FIXTURE_PERSON_A + " 태그", false));
    }

    @Test
    @DisplayName("SUMMARY+SYNTHESIS 동시 단서는 LLM 2차 필요")
    void needsLlmSecondPass_whenSummaryAndSynthesisOverlap() {
        assertTrue(RagIntentClassifier.needsLlmSecondPass("최근 기록의 반복 패턴을 요약해줘"));
        assertTrue(RagIntentClassifier.needsLlmSecondPass("의미를 정리해줘"));
    }

    @Test
    @DisplayName("LOOKUP 강제·단일 의도는 LLM 2차 불필요")
    void needsLlmSecondPass_falseForClearOrLookupForce() {
        assertFalse(RagIntentClassifier.needsLlmSecondPass("패턴 기록 찾아줘"));
        assertFalse(RagIntentClassifier.needsLlmSecondPass("기록을 요약해줘"));
        assertFalse(RagIntentClassifier.needsLlmSecondPass("내 기록의 반복 패턴을 해석해줘"));
        assertFalse(RagIntentClassifier.needsLlmSecondPass("  "));
    }
}
