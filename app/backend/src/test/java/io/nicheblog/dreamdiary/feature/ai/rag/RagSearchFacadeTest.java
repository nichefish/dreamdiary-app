package io.nicheblog.dreamdiary.feature.ai.rag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * RagSearchFacade 의도 파싱 계약 테스트.
 */
class RagSearchFacadeTest {

    /**
     * LLM 응답에서 의도 레이블을 추출한다.
     */
    @Test
    void parseIntentLabel_shouldExtractFirstKnownLabel() {
        final RagSearchFacade facade = new RagSearchFacade(null, null);

        assertEquals(RagIntent.LOOKUP, facade.parseIntentLabel("LOOKUP"));
        assertEquals(RagIntent.SUMMARY, facade.parseIntentLabel("의도: SUMMARY\n설명 생략"));
        assertEquals(RagIntent.SYNTHESIS, facade.parseIntentLabel("I choose SYNTHESIS for this."));
        assertNull(facade.parseIntentLabel("unknown"));
    }
}
