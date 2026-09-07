package io.nicheblog.dreamdiary.feature.ai.person;

import io.nicheblog.dreamdiary.feature.ai.AiRagTestSupport;
import io.nicheblog.dreamdiary.feature.ai.rag.RagContext;
import io.nicheblog.dreamdiary.feature.ai.rag.RagSearchFacade;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import java.lang.reflect.Field;
import java.util.Locale;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PersonSynthesisHybridService 해석 리드·hybrid 프롬프트·RULE_PRIMARY 근거·태도 폴백 계약 테스트.
 */
class PersonSynthesisHybridServiceTest {

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
     * 테스트용 PersonSynthesisHybridService. 실 협력자(PersonFocusResolver·PersonSnapshotService)를 주입한다.
     */
    private static PersonSynthesisHybridService newHybridService() {
        final RagSearchFacade ragSearchFacade = new RagSearchFacade(null, null);
        final PersonFocusResolver personFocusResolver = new PersonFocusResolver(null, null, ragSearchFacade);
        final PersonSnapshotService personSnapshotService = new PersonSnapshotService(personFocusResolver);
        return new PersonSynthesisHybridService(null, personSnapshotService, personFocusResolver);
    }

    /**
     * person-meaning 해석 리드 문단은 태그·역할·기록 유형을 한 문장으로 엮어야 합니다.
     */
    @Test
    void buildPersonMeaningInterpretiveLead_shouldComposeLeadSentence() throws Exception {
        final PersonSynthesisHybridService service = newHybridService();
        final Method method = PersonSynthesisHybridService.class.getDeclaredMethod(
                "buildPersonMeaningInterpretiveLead",
                String.class,
                Map.class,
                List.class,
                Map.class,
                Map.class,
                Map.class
        );
        method.setAccessible(true);

        final String lead = (String) method.invoke(
                service,
                "민수",
                Map.of("[엔서클]#김민수", 3),
                List.of("긴장·경계 축(2)"),
                Map.of("DIARY", 4, "DREAM", 1),
                Map.of("[엔서클]#조직역동", 5),
                Map.of("회고", 4)
        );

        assertTrue(lead.contains("민수"));
        assertTrue(lead.contains("김민수"));
        assertTrue(lead.contains("긴장"));
        assertTrue(lead.contains("일기"));
        assertTrue(lead.contains("조직역동"));
        assertTrue(lead.contains("회고"));
    }

    /**
     * person-stance 해석 리드는 3인칭 주어(민수는) 없이 2인칭 비춤으로 시작해야 합니다.
     */
    @Test
    void buildPersonStanceInterpretiveLead_shouldAvoidThirdPersonSubject() throws Exception {
        final PersonSynthesisHybridService service = newHybridService();
        final Method method = PersonSynthesisHybridService.class.getDeclaredMethod(
                "buildPersonStanceInterpretiveLead",
                String.class,
                Map.class,
                List.class,
                Map.class,
                Map.class,
                Map.class
        );
        method.setAccessible(true);

        final String lead = (String) method.invoke(
                service,
                "민수",
                Map.of("[엔서클]#김민수", 3),
                List.of("긴장·경계 축(2)"),
                Map.of("DIARY", 4, "DREAM", 1),
                Map.of("[엔서클]#조직역동", 5),
                Map.of("회고", 4)
        );

        assertTrue(lead.startsWith("네가 기록에 남긴 태도로 보면,"));
        assertFalse(lead.contains("민수는(는)"));
        assertFalse(lead.contains("기록상 "));
        assertTrue(lead.contains("#김민수"));
        assertTrue(lead.contains("#조직역동"));
    }

    /**
     * interpretive lead는 표시용 태그 포맷([엔서클] 제거)을 써야 합니다.
     */
    @Test
    void buildPersonMeaningInterpretiveLead_shouldUseDisplayTagFormat() throws Exception {
        final PersonSynthesisHybridService service = newHybridService();
        final Method method = PersonSynthesisHybridService.class.getDeclaredMethod(
                "buildPersonMeaningInterpretiveLead",
                String.class,
                Map.class,
                List.class,
                Map.class,
                Map.class,
                Map.class
        );
        method.setAccessible(true);

        final String lead = (String) method.invoke(
                service,
                "지연",
                Map.of("[엔서클]#박지연", 24),
                List.of(),
                Map.of(),
                Map.of("[엔서클]#조직역동", 9),
                Map.of()
        );

        assertTrue(lead.contains("#박지연"));
        assertFalse(lead.contains("[엔서클]"));
    }

    /**
     * 태도 질문 RULE_PRIMARY 근거 장면은 hybrid와 같이 최대 20건까지 실을 수 있어야 합니다.
     */
    @Test
    void appendRulePrimaryEvidenceSection_stanceOptions_shouldIncludeUpToTwentySnippets() throws Exception {
        final PersonSynthesisHybridService service = newHybridService();
        final Class<?> optionsClass = Class.forName(
                "io.nicheblog.dreamdiary.feature.ai.person.PersonSnapshotService$PersonMeaningSnapshotOptions"
        );
        final Method stanceRich = optionsClass.getDeclaredMethod("personStanceRich");
        stanceRich.setAccessible(true);
        final Object stanceOptions = stanceRich.invoke(null);

        final Method method = PersonSynthesisHybridService.class.getDeclaredMethod(
                "appendRulePrimaryEvidenceSection",
                StringBuilder.class,
                List.class,
                optionsClass
        );
        method.setAccessible(true);

        final StringBuilder sb = new StringBuilder();
        method.invoke(
                service,
                sb,
                List.of(
                        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                        "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V"
                ),
                stanceOptions
        );

        final String out = sb.toString();
        assertTrue(out.contains("근거 장면:"));
        assertTrue(out.contains("T"));
        assertFalse(out.contains(" | U"));
    }

    /**
     * hybrid 시스템 프롬프트는 SNAPSHOT 블록과 표시용 태그를 포함해야 합니다.
     */
    @Test
    void buildPersonSynthesisHybridSystemPrompt_shouldIncludeSnapshotWithDisplayTags() throws Exception {
        final PersonSynthesisHybridService service = newHybridService();

        final RagContext ragContext = (RagContext) AiRagTestSupport.buildTestRagContextWithTaggedResults("민수");
        final String prompt = service.buildHybridSystemPrompt(
                ragContext.personFocus(),
                ragContext.results(),
                "나는 민수님을 어떻게 생각하고 있니?"
        );

        assertTrue(prompt.contains("PERSON_SYNTHESIS_HYBRID"));
        assertTrue(prompt.contains("SNAPSHOT"));
        assertTrue(prompt.contains("네가 기록에 남긴 바로는"));
        assertTrue(prompt.contains("형식은 자유"));
        assertTrue(prompt.contains("근거 장면을 최대한 많이"));
        assertFalse(prompt.contains("네 섹션 헤더"));
        assertFalse(prompt.contains("[엔서클]"));
    }

    /**
     * RULE_PRIMARY fallback 근거 장면은 hybrid와 같이 최대 3건을 | 로 이어야 합니다.
     */
    @Test
    void appendRulePrimaryEvidenceSection_shouldIncludeUpToThreeSnippets() throws Exception {
        final PersonSynthesisHybridService service = newHybridService();

        final StringBuilder sb = new StringBuilder();
        service.appendRulePrimaryEvidenceSection(sb, List.of("장면A", "장면B", "장면C", "장면D"));

        final String out = sb.toString();
        assertTrue(out.contains("근거 장면(짧게):"));
        assertTrue(out.contains("장면A"));
        assertTrue(out.contains("장면B"));
        assertTrue(out.contains("장면C"));
        assertFalse(out.contains("장면D"));
        assertTrue(out.contains(" | "));
    }

    /**
     * RULE_PRIMARY 근거 장면은 대화 인용·화자 라벨을 제거해야 합니다.
     */
    @Test
    void compactRulePrimaryEvidenceSnippet_shouldStripDialogueQuotes() throws Exception {
        final PersonSynthesisHybridService service = newHybridService();

        final String compact = service.compactRulePrimaryEvidenceSnippet(
                "... \"예상과 달랐다고\" \"말했다\" 지연님: \"오전 회의\" 나: \"자료 확인\" 민수님은 옆에서 지켜봤고."
        );

        assertFalse(compact.contains("\""));
        assertFalse(compact.contains("지연님:"));
        assertTrue(compact.contains("민수"));
    }

    /**
     * 태도 결정론 폴백(rich-trust)은 4섹션 태그 덤프 대신 2인칭 산문 근거 노트를 만들어야 합니다.
     *
     * <p>해석 리드로 연결 맥락 태그를 근거로 인용하고, 기록에 없는 건 단정하지 않는다는 문구를 남기되,
     * 예전 (1)~(4) 섹션 헤더 형식은 재도입하지 않는다.</p>
     */
    @Test
    void buildPersonStanceDeterministicFallback_shouldWriteGroundedSecondPersonProse() throws Exception {
        final PersonSynthesisHybridService service = newHybridService();

        final RagContext ragContext = (RagContext) AiRagTestSupport.buildTestRagContextWithTaggedResults("민수");
        final String fallback = service.buildPersonStanceDeterministicFallback(
                ragContext.personFocus(),
                ragContext.results(),
                "나는 민수님을 어떻게 생각하고 있니?"
        );

        assertTrue(fallback.contains("네가 기록에 남긴"));
        assertTrue(fallback.contains("조직역동"));
        assertTrue(fallback.contains("단정하기 어려워"));
        assertFalse(fallback.contains("(1) 내 태도·정서"));
        assertFalse(fallback.contains("(4) 확정 불가"));
    }
}
