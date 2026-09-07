package io.nicheblog.dreamdiary.feature.ai.person;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PersonQueryClassifier 인물 질문 분류·토큰 추출 계약 테스트.
 */
class PersonQueryClassifierTest {

    /**
     * 인물 의미 질문에서는 조사 제거 후 이름 token 자체가 남아야 합니다.
     */
    @Test
    void extractPersonFocusTokens_shouldKeepNamedPersonToken() {
        final List<String> tokens =
                PersonQueryClassifier.extractPersonFocusTokens("민수는 내 Dreamdiary 기록에서 어떤 의미로 등장해?");

        assertEquals(List.of("민수"), tokens);
    }

    /**
     * 호칭(님)이 붙은 인물 표현은 태그 검색용 토큰에서 제거되어야 합니다.
     */
    @Test
    void extractPersonFocusTokens_shouldStripHonorificSuffix() {
        final List<String> tokens =
                PersonQueryClassifier.extractPersonFocusTokens("나는 민수님을 어떻게 생각하고 있니?");

        assertTrue(tokens.contains("민수"));
        assertFalse(tokens.contains("민수님"));
    }

    /**
     * appearance 질문 토큰 추출 시 범위어(대화)보다 인물명(지연)을 남겨야 합니다.
     */
    @Test
    void extractPersonFocusTokens_shouldPreferPersonNameOverDialogueScopeWord() {
        final List<String> tokens =
                PersonQueryClassifier.extractPersonFocusTokens("내 대화에서 지연님은 어떤 느낌으로 등장하고 있니");

        assertTrue(tokens.contains("지연"));
        assertFalse(tokens.contains("대화"));
    }

    /**
     * person-meaning 힌트는 태도/감정 질문도 인식해야 합니다.
     */
    @Test
    void isPersonMeaningQuery_shouldRecognizeHowYouThinkQuestion() {
        assertTrue(PersonQueryClassifier.isPersonMeaningQuery("나는 민수님을 어떻게 생각하고 있니?"));
    }

    /**
     * '느끼고' 태도 질문도 person-meaning으로 인식해 Path C 조건(SYNTHESIS+meaning)을 만족해야 합니다(③ 라우팅 수렴).
     */
    @Test
    void isPersonMeaningQuery_shouldRecognizeFeelVerbAttitude() {
        assertTrue(PersonQueryClassifier.isPersonMeaningQuery("나는 민수이를 어떻게 느끼고 있지?"));
    }

    /**
     * '~에 대해 뭘 말해' 류 인물 질문은 person-meaning 경로로 인식해야 합니다.
     */
    @Test
    void isPersonMeaningQuery_shouldRecognizePersonAboutQuestion() {
        assertTrue(PersonQueryClassifier.isPersonMeaningQuery("민수님에 대해 뭘 말해줘 수 있니?"));
    }

    /**
     * person-about LOOKUP 힌트는 뭘/무엇을 말해·알려 표현을 인식해야 합니다.
     */
    @Test
    void isPersonAboutLookupQuery_shouldRecognizeFixedLookupHints() {
        assertTrue(PersonQueryClassifier.isPersonAboutLookupQuery("민수님에 대해 무엇을 말해줄 수 있니?"));
        assertTrue(PersonQueryClassifier.isPersonAboutLookupQuery("민수님에 대해 뭘 알려줘"));
        assertFalse(PersonQueryClassifier.isPersonAboutLookupQuery("오늘 꿈 해석해줘"));
    }
}
