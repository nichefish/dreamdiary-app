package io.nicheblog.dreamdiary.feature.chat.service;

import io.nicheblog.dreamdiary.feature.chat.model.RagIntent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ChatAIService 프롬프트 계약 테스트.
 */
class ChatAIServiceTest {

    /**
     * 통섭형 인물 질문은 기록 근거 없는 현실 관계 지위 추정을 막아야 합니다.
     */
    @Test
    void buildIntentPrompt_shouldConstrainPersonRoleInferenceForSynthesis() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod("buildIntentPrompt", RagIntent.class);
        method.setAccessible(true);

        final String prompt = (String) method.invoke(service, RagIntent.SYNTHESIS);

        assertTrue(prompt.contains("직장 상사/동료/연인/가족"));
        assertTrue(prompt.contains("기록에 직접 나온 표현이 있을 때만"));
        assertTrue(prompt.contains("기록상 확인되는 등장 방식"));
        assertTrue(prompt.contains("아직 확정할 수 없는 점"));
    }
}
