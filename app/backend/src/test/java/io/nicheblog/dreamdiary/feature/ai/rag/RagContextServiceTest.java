package io.nicheblog.dreamdiary.feature.ai.rag;

import io.nicheblog.dreamdiary.feature.ai.client.OllamaClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * RagContextService 의도 분류·검색 폭 계약 테스트.
 */
class RagContextServiceTest {

    /** LLM 2차 분류를 트리거하는 테스트용 비어있지 않은 시스템 프롬프트. */
    private static final String INTENT_CLASSIFY_PROMPT = "intent-classify-system-prompt";

    /** 관리자 RAG 검색 한도 코드 기본값(synthesisTopK=25, stanceTopK=50). */
    private static final RagSearchLimits DEFAULT_LIMITS =
            new RagSearchLimits(true, 5, 0.35D, 12, 25, 50, 0.25D);

    private static RagContextService newService() {
        return newService(null);
    }

    private static RagContextService newService(final OllamaClient ollama) {
        return new RagContextService(new RagSearchFacade(null, ollama), null, null, null, null);
    }

    /**
     * "어떻게 생각" 류 질문은 통섭형(SYNTHESIS)으로 분류해야 합니다.
     */
    @Test
    void detectIntent_shouldTreatAttitudeQuestionAsSynthesis() {
        assertEquals(
                RagIntent.SYNTHESIS,
                newService().detectIntent("나는 민수님을 어떻게 생각하고 있니?", null)
        );
    }

    /**
     * '느끼고'/'어떻게 느끼' 표현의 태도 질문도 SYNTHESIS로 수렴해야 합니다(③ 라우팅 수렴).
     *
     * <p>이 표현이 SYNTHESIS로 분류돼야 Path C(rich-trust)로 라우팅되고, 레거시 LOOKUP 저하 경로로
     * 새지 않는다.</p>
     */
    @Test
    void detectIntent_shouldTreatFeelVerbAttitudeAsSynthesis() {
        assertEquals(
                RagIntent.SYNTHESIS,
                newService().detectIntent("나는 민수이를 어떻게 느끼고 있지?", null)
        );
    }

    /**
     * 인물 about 질문은 SYNTHESIS intent로 분류해야 합니다.
     */
    @Test
    void detectIntent_shouldTreatPersonAboutQuestionAsSynthesis() {
        assertEquals(
                RagIntent.SYNTHESIS,
                newService().detectIntent("민수님에 대해 뭘 말해줘 수 있니?", null)
        );
    }

    /**
     * SUMMARY+SYNTHESIS 모호 질문은 LLM 2차 레이블을 우선한다.
     */
    @Test
    void detectIntent_shouldPreferLlmWhenAmbiguous() {
        final OllamaClient ollama = Mockito.mock(OllamaClient.class);
        when(ollama.chat(anyString(), anyString())).thenReturn("SUMMARY");

        assertEquals(
                RagIntent.SUMMARY,
                newService(ollama).detectIntent("의미를 정리해줘", INTENT_CLASSIFY_PROMPT)
        );
    }

    /**
     * LLM 실패 시 휴리스틱(SYNTHESIS 우선)으로 돌아간다.
     */
    @Test
    void detectIntent_shouldFallbackToHeuristicWhenLlmFails() {
        final OllamaClient ollama = Mockito.mock(OllamaClient.class);
        when(ollama.chat(anyString(), anyString())).thenThrow(new IllegalStateException("ollama down"));

        assertEquals(
                RagIntent.SYNTHESIS,
                newService(ollama).detectIntent("의미를 정리해줘", INTENT_CLASSIFY_PROMPT)
        );
    }

    /**
     * 태도 질문 검색 폭은 tag-only·merged 경로 공통으로 확대 폭(50)을 써야 합니다(F3 정렬).
     *
     * <p>일반 SYNTHESIS 질문은 기본 폭(25)을 유지한다. merged 폴백 경로가 queryText를 넘기지 않아
     * 태도 질문이 기본 폭으로 좁아지던 불일치를 고정하는 계약.</p>
     */
    @Test
    void resolveTopK_shouldUseStanceBudgetForAttitudeQuery() {
        final RagContextService service = newService();

        final int stanceTopK = service.resolveTopK(RagIntent.SYNTHESIS, "나는 민수님을 어떻게 생각하고 있니?", DEFAULT_LIMITS);
        final int synthesisTopK = service.resolveTopK(RagIntent.SYNTHESIS, "내 기록의 반복 패턴을 해석해줘", DEFAULT_LIMITS);

        assertEquals(50, stanceTopK);
        assertEquals(25, synthesisTopK);
    }
}
