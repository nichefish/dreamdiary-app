package io.nicheblog.dreamdiary.feature.ai.prompt;

import io.nicheblog.dreamdiary.feature.ai.rag.RagIntent;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import java.lang.reflect.Field;
import java.util.Locale;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * IntentPromptResolver 의도별 추가 프롬프트 계약 테스트.
 */
class IntentPromptResolverTest {

    @BeforeAll
    static void bindMessageSourceForChatCatalog() throws Exception {
        final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("classpath:messages/messages");
        messageSource.setDefaultEncoding("UTF-8");
        final Field field = MessageUtils.class.getDeclaredField("messageSource");
        field.setAccessible(true);
        field.set(null, messageSource);
        LocaleContextHolder.setLocale(Locale.KOREAN);
    }

    /**
     * 통섭형 인물 질문은 역할 추정 억제 지시를 포함하고, 제거된 스캐폴드 블록을 참조하지 않아야 합니다.
     *
     * <p>PERSON_FOCUS·PERSON_MEANING_SCAFFOLD 블록은 convergence로 제거됨 — personFocus가 해결된
     * 질문은 Path C(SNAPSHOT)로 가고, 이 레거시 프롬프트가 쓰이는 경로에서는 블록이 존재할 수 없다.</p>
     */
    @Test
    void buildIntentPrompt_shouldConstrainPersonRoleInferenceForSynthesis() {
        final IntentPromptResolver resolver = new IntentPromptResolver();

        final String prompt = resolver.buildIntentPrompt(RagIntent.SYNTHESIS, null);

        assertTrue(prompt.contains("업무 협업"));
        assertTrue(prompt.contains("현실 관계 지위는 기록에 직접 나온 표현이 있을 때만"));
        assertFalse(prompt.contains("PERSON_FOCUS"));
        assertFalse(prompt.contains("PERSON_MEANING_SCAFFOLD"));
    }

    /**
     * LOOKUP 인물 질문 프롬프트는 조직 일반론 억제와 인덱스 인용 금지를 포함해야 합니다.
     */
    @Test
    void buildIntentPrompt_shouldConstrainLookupPersonQueries() {
        final IntentPromptResolver resolver = new IntentPromptResolver();

        final String prompt = resolver.buildIntentPrompt(RagIntent.LOOKUP, "민수님은 뭐 했어?");

        assertTrue(prompt.contains("조직"));
        assertTrue(prompt.contains("[1]"));
    }

    /**
     * 대화 등장 질문은 appearance 전용 intent 프롬프트를 써야 합니다.
     */
    @Test
    void buildIntentPrompt_shouldUseAppearanceBranchForDialogueQuestion() {
        final IntentPromptResolver resolver = new IntentPromptResolver();

        final String prompt = resolver.buildIntentPrompt(
                RagIntent.SYNTHESIS,
                "내 대화에서 지연님은 어떤 느낌으로 등장하고 있니"
        );

        assertTrue(prompt.contains("등장"));
        assertTrue(prompt.contains("추론하자면"));
        assertFalse(prompt.contains("PERSON_MEANING_SCAFFOLD"));
        assertFalse(prompt.contains("PERSON_STANCE_SCAFFOLD"));
    }

    /**
     * SYNTHESIS 태도 질문 intent 프롬프트는 rich-trust(자유 산문·2인칭 비춤·반환각만 금지)여야 합니다.
     *
     * <p>이 분기는 personFocus 미해결(기록에 단서 없는 인물) 레거시 경로에서만 쓰이며,
     * 예전 PERSON_STANCE_SCAFFOLD 골격·조언/톤 금지 레짐은 재도입하지 않는다.</p>
     */
    @Test
    void buildIntentPrompt_shouldUseRichTrustProseForAttitudeQuestion() {
        final IntentPromptResolver resolver = new IntentPromptResolver();

        final String prompt = resolver.buildIntentPrompt(
                RagIntent.SYNTHESIS,
                "나는 민수님을 어떻게 생각하고 있니?"
        );

        assertTrue(prompt.contains("네가 기록에 남긴 바로는"));
        assertTrue(prompt.contains("형식은 자유"));
        assertTrue(prompt.contains("기록만으로는 확실치 않다"));
        assertFalse(prompt.contains("PERSON_STANCE_SCAFFOLD"));
        assertFalse(prompt.contains("고려할 수 있"));
    }
}
