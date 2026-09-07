package io.nicheblog.dreamdiary.feature.chat.service;

import io.nicheblog.dreamdiary.feature.ai.AiRagTestSupport;
import io.nicheblog.dreamdiary.feature.ai.client.OllamaClient;
import io.nicheblog.dreamdiary.feature.ai.rag.RagContext;
import io.nicheblog.dreamdiary.feature.ai.rag.RagContextService;
import io.nicheblog.dreamdiary.feature.ai.rag.RagContextTextBuilder;
import io.nicheblog.dreamdiary.feature.ai.rag.RagIntent;
import io.nicheblog.dreamdiary.feature.ai.person.PersonFocusResolver;
import io.nicheblog.dreamdiary.feature.ai.guard.ResponseGuardService;
import io.nicheblog.dreamdiary.feature.ai.prompt.IntentPromptResolver;
import io.nicheblog.dreamdiary.feature.ai.prompt.SystemPromptBuilder;
import io.nicheblog.dreamdiary.feature.ai.person.PersonSnapshotService;
import io.nicheblog.dreamdiary.feature.ai.person.PersonSynthesisHybridService;
import io.nicheblog.dreamdiary.feature.ai.rag.RagSearchFacade;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ChatOrchestrator 프롬프트 계약 테스트.
 */
class ChatOrchestratorTest {





    /**
     * 테스트용 ChatOrchestrator. RAG 의도·병합은 {@link RagSearchFacade}에 위임하므로 facade를 항상 주입한다.
     */
    private static ChatOrchestrator newService() {
        return newService(null);
    }

    private static ChatOrchestrator newService(final OllamaClient ollama) {
        final RagSearchFacade ragSearchFacade = new RagSearchFacade(null, ollama);
        final PersonFocusResolver personFocusResolver = new PersonFocusResolver(null, null, ragSearchFacade);
        final PersonSnapshotService personSnapshotService = new PersonSnapshotService(personFocusResolver);
        final PersonSynthesisHybridService personSynthesisHybridService = new PersonSynthesisHybridService(
                ollama,
                personSnapshotService,
                personFocusResolver
        );
        final ResponseGuardService responseGuardService = new ResponseGuardService(personSnapshotService);
        final IntentPromptResolver intentPromptResolver = new IntentPromptResolver();
        final SystemPromptBuilder systemPromptBuilder = new SystemPromptBuilder(intentPromptResolver);
        final RagContextTextBuilder ragContextTextBuilder = new RagContextTextBuilder(
                personFocusResolver,
                personSnapshotService
        );
        final RagContextService ragContextService = new RagContextService(
                ragSearchFacade,
                null,
                null,
                personFocusResolver,
                ragContextTextBuilder
        );
        final RagMetadataJsonBuilder ragMetadataJsonBuilder =
                new RagMetadataJsonBuilder(ragContextService, personSnapshotService, personFocusResolver);
        return new ChatOrchestrator(
                null,
                null,
                null,
                ollama,
                ragContextService,
                personSynthesisHybridService,
                responseGuardService,
                systemPromptBuilder,
                intentPromptResolver,
                null,
                ragMetadataJsonBuilder
        );
    }

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
     * person-meaning fallback은 연결 맥락 태그와 챕터 말머리를 포함해야 합니다.
     */
    @Test
    void buildPersonMeaningDeterministicFallback_shouldIncludeLinkedContextAndChapter() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "buildPersonMeaningDeterministicFallback",
                RagContext.class
        );
        method.setAccessible(true);

        final Object ragContext = AiRagTestSupport.buildTestRagContextWithTaggedResults("민수");
        final String fallback = (String) method.invoke(service, ragContext);

        assertTrue(fallback.contains("연결 맥락"));
        assertTrue(fallback.contains("조직역동"));
        assertTrue(fallback.contains("챕터 말머리"));
        assertTrue(fallback.contains("회고"));
        assertFalse(fallback.toLowerCase().contains("entity catalog"));
    }


    /**
     * 응답 후처리는 RAG 내부 기록 인덱스 인용을 제거해야 합니다.
     */
    @Test
    void stripInternalRecordCitations_shouldRemoveBracketIndexes() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod("stripInternalRecordCitations", String.class);
        method.setAccessible(true);

        final String cleaned = (String) method.invoke(
                service,
                "예를 들어, [2] 기록에서는 개입도가 높았습니다."
        );

        assertFalse(cleaned.contains("[2]"));
        assertTrue(cleaned.contains("기록"));
    }

    /**
     * LOOKUP 인물 태도 질문의 빈 조직 일반론은 degraded로 감지해야 합니다.
     */
    @Test
    void isDegradedPersonResponse_shouldFlagLookupGenericBucketAnswer() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "isDegradedPersonResponse",
                String.class,
                RagContext.class,
                String.class
        );
        method.setAccessible(true);

        final Class<?> ragContextClass = Class.forName(
                RagContext.class.getName()
        );
        final Class<?> personFocusClass = Class.forName(
                "io.nicheblog.dreamdiary.feature.ai.person.PersonFocus"
        );
        final var lookupCtor = ragContextClass.getDeclaredConstructor(
                RagIntent.class,
                List.class,
                String.class,
                personFocusClass
        );
        lookupCtor.setAccessible(true);
        final Object lookupContext = lookupCtor.newInstance(RagIntent.LOOKUP, List.of(), null, null);

        final boolean degraded = (boolean) method.invoke(
                service,
                "민수님은 조직 내에서 중요한 역할을 하는 인물이에요.",
                lookupContext,
                "나는 민수님을 어떻게 생각하고 있니?"
        );

        assertTrue(degraded);
    }

    /**
     * 1인칭 태도 질문은 person-attitude 경로로 분류해야 합니다.
     */
    @Test
    void isPersonAttitudeQuery_shouldRecognizeHowYouThinkQuestion() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod("isPersonAttitudeQuery", String.class);
        method.setAccessible(true);

        final boolean attitude = (boolean) method.invoke(
                service,
                "나는 민수님을 어떻게 생각하고 있니?"
        );

        assertTrue(attitude);
    }

    /**
     * 상징·의미 질문(1인칭 태도 아님)은 person-attitude로 분류하지 않아야 합니다.
     */
    @Test
    void isPersonAttitudeQuery_shouldNotMatchSymbolicMeaningQuestion() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod("isPersonAttitudeQuery", String.class);
        method.setAccessible(true);

        final boolean attitude = (boolean) method.invoke(
                service,
                "민수는 내 기록에서 어떤 의미야?"
        );

        assertFalse(attitude);
    }

    /**
     * 내 대화/등장 질문은 person-attitude가 아니어야 합니다.
     */
    @Test
    void isPersonAttitudeQuery_shouldNotMatchDialogueAppearanceQuestion() throws Exception {
        final ChatOrchestrator service = newService();
        final Method attitudeMethod = ChatOrchestrator.class.getDeclaredMethod("isPersonAttitudeQuery", String.class);
        final Method appearanceMethod = ChatOrchestrator.class.getDeclaredMethod("isPersonAppearanceQuery", String.class);
        attitudeMethod.setAccessible(true);
        appearanceMethod.setAccessible(true);

        final String query = "내 대화에서 지연님은 어떤 느낌으로 등장하고 있니";
        final boolean attitude = (boolean) attitudeMethod.invoke(service, query);
        final boolean appearance = (boolean) appearanceMethod.invoke(service, query);

        assertFalse(attitude);
        assertTrue(appearance);
    }

    /**
     * 인용 나열+성격 단정 person-meaning 답변은 degraded로 감지해야 합니다.
     */
    @Test
    void isDegradedPersonResponse_shouldFlagTraitQuoteParadeForAppearanceQuestion() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "isDegradedPersonResponse",
                String.class,
                RagContext.class,
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = AiRagTestSupport.buildTestRagContextWithTaggedResults("지연");
        final String shallowResponse =
                "기록상 #박지연 축에 묶여 있고, \"점심 메뉴를 물었다\" 또는 \"회의실로 가자고 했다\" 등의 말을 하면서 등장합니다. "
                        + "이로 부터 추론하자면, 친근하고 자연스러운 인물로 등장하는 것 같아.";

        final boolean degraded = (boolean) method.invoke(
                service,
                shallowResponse,
                ragContext,
                "내 대화에서 지연님은 어떤 느낌으로 등장하고 있니"
        );

        assertTrue(degraded);
    }

    /**
     * 태도 질문에 대한 HR식 성격 평가 답변은 degraded로 감지해야 합니다.
     */
    @Test
    void isDegradedPersonResponse_shouldFlagHrProfileForAttitudeQuestion() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "isDegradedPersonResponse",
                String.class,
                RagContext.class,
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = AiRagTestSupport.buildTestRagContextWithTaggedResults("민수");

        final boolean degraded = (boolean) method.invoke(
                service,
                "민수님은 매우 열성적이고 주동적인 인물입니다. "
                        + "조직 역동성에서 중요한 역할을 하며 협업 관계를 유지하는 것이 중요할 것 같습니다.",
                ragContext,
                "나는 민수님을 어떻게 생각하고 있니?"
        );

        assertTrue(degraded);
    }

    /**
     * 풍부 신뢰(Option A) 모드: 기록에 근거한 사건 나열·심리 해석 태도 답변은 더 이상 거부하지 않습니다.
     */
    @Test
    void isDegradedPersonResponse_shouldAcceptEpisodeNarrationInRichMode() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "isDegradedPersonResponse",
                String.class,
                RagContext.class,
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = AiRagTestSupport.buildTestRagContextWithTaggedResults("민수");
        final String episodeResponse =
                "네가 기록에 남긴 바로는, 민수님에 대한 태도는 그리 긍정적이지 않아 보인다. "
                        + "퇴근 시간이었는데 PDF 파싱 이야기를 나누는 동안 단가가 안 나올 것 같다고 말했다. "
                        + "그러자 민수님은 방어적으로 대답했고, 특히 네 마음에는 불신과 거리감이 있는 것 같다. "
                        + "어줘든 이는 기록에 남긴 대화만으로 추론한 것이다.";

        final boolean degraded = (boolean) method.invoke(
                service,
                episodeResponse,
                ragContext,
                "나는 민수님을 어떻게 생각하고 있니?"
        );

        assertFalse(degraded);
    }

    /**
     * person-stance 답변이 태그·축을 인용하면 episode-only 거부 대상이 아니어야 합니다.
     */
    @Test
    void isDegradedPersonResponse_shouldAcceptAxisGroundedStanceAnswer() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "isDegradedPersonResponse",
                String.class,
                RagContext.class,
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = AiRagTestSupport.buildTestRagContextWithTaggedResults("민수");
        final String groundedResponse =
                "네가 기록에 남긴 바로는, 민수에 대한 마음은 #조직역동 축에서 자주 긴장과 우려가 반복돼. "
                        + "(1) 내 태도·정서: 기록을 보면 우려가 드러남. "
                        + "(2) 반복 패턴: 업무 논의 장면에서 답이 어길리는 모습이 반복. "
                        + "(3) 함께 묶인 축: #김민수, #조직역동. "
                        + "(4) 확정 불가: 상대 성격은 기록에 없음.";

        final boolean degraded = (boolean) method.invoke(
                service,
                groundedResponse,
                ragContext,
                "나는 민수님을 어떻게 생각하고 있니?"
        );

        assertFalse(degraded);
    }

    /**
     * 풍부 신뢰(Option A) 모드: 조언·중립화 톤이라도 기록 태그를 인용하면 거부하지 않습니다.
     * (톤 검열 대신 기록 근거 없는 빈 버킷만 거부하는 최소 게이트로 전환.)
     */
    @Test
    void isDegradedPersonResponse_shouldAcceptAdvisoryStyleWhenTagGroundedInRichMode() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "isDegradedPersonResponse",
                String.class,
                RagContext.class,
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = AiRagTestSupport.buildTestRagContextWithTaggedResults("민수");
        final String advisoryResponse =
                "dreamdiary 기록을 통해 알 수 있는 바로, 당신은 민수님과 함께 일하면서 다양한 상황에서 교류를 가집니다. "
                        + "그러나 직접적인 평가나 심리 상태는 명확히 나타나지 않습니다. "
                        + "이러한 관계를 더 깊게 이해하기 위해서는 몇 가지 점을 고려할 수 있습니다: "
                        + "상호작용 패턴: #조직역동 축에 묶여 있습니다. "
                        + "확정 불가: 당신의 생각이나 감정은 명시적으로 표현되지 않고, 중립적 또는 평온한 태도를 유지하고 있는 것으로 보입니다.";

        final boolean degraded = (boolean) method.invoke(
                service,
                advisoryResponse,
                ragContext,
                "나는 민수님을 어떻게 생각하고 있니?"
        );

        assertFalse(degraded);
    }

    /**
     * SYNTHESIS + personFocus + person-meaning 질문은 RULE_PRIMARY 경로를 써야 합니다.
     */
    @Test
    void shouldUseRulePrimaryPersonSynthesisResponse_shouldBeTrueForAttitudeQuestion() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "shouldUseRulePrimaryPersonSynthesisResponse",
                RagContext.class,
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = AiRagTestSupport.buildTestRagContext("민수");
        final boolean rulePrimary = (boolean) method.invoke(
                service,
                ragContext,
                "나는 민수님을 어떻게 생각하고 있니?"
        );

        assertTrue(rulePrimary);
    }

    /**
     * personFocus 없는 SYNTHESIS 질문은 RULE_PRIMARY를 쓰지 않아야 합니다.
     */
    @Test
    void shouldUseRulePrimaryPersonSynthesisResponse_shouldBeFalseWithoutPersonFocus() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "shouldUseRulePrimaryPersonSynthesisResponse",
                RagContext.class,
                String.class
        );
        method.setAccessible(true);

        final Class<?> ragContextClass = RagContext.class;
        final var ragContextCtor = ragContextClass.getDeclaredConstructor(
                RagIntent.class,
                List.class,
                String.class,
                Class.forName("io.nicheblog.dreamdiary.feature.ai.person.PersonFocus")
        );
        ragContextCtor.setAccessible(true);
        final Object ragContext = ragContextCtor.newInstance(RagIntent.SYNTHESIS, List.of(), "ctx", null);

        final boolean rulePrimary = (boolean) method.invoke(
                service,
                ragContext,
                "민수는 내 기록에서 어떤 의미야?"
        );

        assertFalse(rulePrimary);
    }

    /**
     * appearance fallback은 4단 구조와 표시용 태그를 포함해야 합니다.
     */
    @Test
    void buildPersonAppearanceDeterministicFallback_shouldUseFourSectionShape() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "buildPersonAppearanceDeterministicFallback",
                RagContext.class
        );
        method.setAccessible(true);

        final Object ragContext = AiRagTestSupport.buildTestRagContextWithTaggedResults("지연");
        final String response = (String) method.invoke(service, ragContext);

        assertTrue(response.contains("(1) 등장 느낌"));
        assertTrue(response.contains("(2) 반복 맥락"));
        assertTrue(response.contains("(3) 함께 묶인 축"));
        assertTrue(response.contains("(4) 확정 불가"));
        assertFalse(response.contains("[엔서클]"));
    }

    /**
     * 풍부 신뢰(Option A) 모드: 3인칭 서술이 섞여도 #태그 근거가 있으면 태도 답변을 거부하지 않습니다.
     */
    @Test
    void isDegradedPersonResponse_shouldAcceptTagGroundedNarrativeInRichMode() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "isDegradedPersonResponse",
                String.class,
                RagContext.class,
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = AiRagTestSupport.buildTestRagContextWithTaggedResults("민수");
        final String hybridStyleResponse =
                "네가 기록에 남긴 바로는, 김민수는 주로 조직 내 역동과 관련된 상황에서 언급되는 경향이 있다. "
                        + "특히 #박지연이라는 인물과 함께 자주 등장한다. "
                        + "예를 들어 \"예상과 달랐어요\" 와 같은 문장은 민수에 대한 개인적 기대감이나 실망감을 드러낸다. "
                        + "김민수와 함께 묶인 축으로는 #조직역동, #박지연 등이 있다. "
                        + "확실하지 않은 점으로는 민수가 조직 내에서 어떤 위치를 차지하는지는 더 자세히 알기 어렵다.";

        final boolean degraded = (boolean) method.invoke(
                service,
                hybridStyleResponse,
                ragContext,
                "나는 민수님을 어떻게 생각하고 있니?"
        );

        assertFalse(degraded);
    }

    /**
     * 4섹션 형식과 사용자 태도 비추가 있으면 person-stance 답은 통과해야 합니다.
     */
    @Test
    void isDegradedPersonResponse_shouldAcceptStructuredStanceMirror() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "isDegradedPersonResponse",
                String.class,
                RagContext.class,
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = AiRagTestSupport.buildTestRagContextWithTaggedResults("민수");
        final String structuredResponse =
                "네가 기록에 남긴 바로는, #김민수 축에서 기대와 실망이 반복된다.\n"
                        + "(1) 내 태도·정서: 네가 민수에 대해 기대감을 적어 두고 있다.\n"
                        + "(2) 반복 패턴: #김민수와 #조직역동이 잡히 등장한다.\n"
                        + "(3) 함께 묶인 축: #박지연, #조직역동\n"
                        + "(4) 확정 불가: 조직 역할을 단정할 규거는 부족하다.";

        final boolean degraded = (boolean) method.invoke(
                service,
                structuredResponse,
                ragContext,
                "나는 민수님을 어떻게 생각하고 있니?"
        );

        assertFalse(degraded);
    }
    /**
     * #조직역동·4섹션·mirror가 있는 조직 축 언급은 coaching/org 가드에 걸리지 않아야 합니다.
     */
    @Test
    void isDegradedPersonResponse_shouldAcceptOrgPhraseWhenTagGrounded() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "isDegradedPersonResponse",
                String.class,
                RagContext.class,
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = AiRagTestSupport.buildTestRagContextWithTaggedResults("민수");
        final String groundedResponse =
                "네가 기록에 남긴 바로는, 조직 내 역동 맥락에서 #조직역동과 #김민수가 반복된다.\n"
                        + "(1) 내 태도·정서: 기대와 실망이 교차한다.\n"
                        + "(2) 반복 패턴: #조직역동, #박지연\n"
                        + "(3) 함께 묶인 축: #김민수\n"
                        + "(4) 확정 불가: 조직 역할 단정 불가";

        final boolean degraded = (boolean) method.invoke(
                service,
                groundedResponse,
                ragContext,
                "나는 민수님을 어떻게 생각하고 있니?"
        );

        assertFalse(degraded);
    }

    /**
     * person 재시도 프롬프트는 1차 가드 실패 guardDetail을 포함해야 합니다.
     */
    @Test
    void buildPersonMeaningRetryPrompt_shouldIncludeGuardDetailHint() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "buildPersonMeaningRetryPrompt",
                RagContext.class,
                String.class,
                String.class
        );
        method.setAccessible(true);

        final String stancePrompt = (String) method.invoke(
                service,
                null,
                "나는 민수님을 어떻게 생각하고 있니?",
                "person_stance_generic_bucket"
        );
        assertTrue(stancePrompt.contains("PERSON_STANCE_RETRY"));
        assertTrue(stancePrompt.contains("person_stance_generic_bucket"));
        assertTrue(stancePrompt.contains("근거 장면"));
        assertFalse(stancePrompt.contains("4섹션"));

        final String meaningPrompt = (String) method.invoke(
                service,
                null,
                "민수님은 내 기록에서 어떤 의미야?",
                "person_meaning_hollow"
        );
        assertTrue(meaningPrompt.contains("PERSON_MEANING_RETRY"));
        assertTrue(meaningPrompt.contains("person_meaning_hollow"));
    }





    /**
     * 한국어 locale에서 language-retry 프롬프트는 한글 중심 재작성 지시를 포함해야 한다.
     */
    @Test
    void languageRetryPrompt_shouldLoadKoreanCatalog() throws Exception {
        LocaleContextHolder.setLocale(Locale.KOREAN);
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod("languageRetryPrompt");
        method.setAccessible(true);

        final String prompt = (String) method.invoke(service);

        assertNotNull(prompt);
        assertTrue(prompt.contains("한글"));
        assertTrue(prompt.contains("중국어") || prompt.contains("한자"));
        assertFalse(prompt.contains("Rewrite the answer in English only"));
    }

    /**
     * 영어 locale에서 language-retry 프롬프트는 영어 전용 재작성 지시를 포함해야 한다.
     */
    @Test
    void languageRetryPrompt_shouldLoadEnglishCatalog() throws Exception {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        try {
            final ChatOrchestrator service = newService();
            final Method method = ChatOrchestrator.class.getDeclaredMethod("languageRetryPrompt");
            method.setAccessible(true);

            final String prompt = (String) method.invoke(service);

            assertNotNull(prompt);
            assertTrue(prompt.contains("English only"));
            assertTrue(prompt.contains("Han") || prompt.contains("Chinese"));
        } finally {
            LocaleContextHolder.setLocale(Locale.KOREAN);
        }
    }

    /**
     * 한자 1자는 허용하고 2자 이상부터 Korean-only 가드 위반으로 본다.
     */
    @Test
    void containsDisallowedHanScript_shouldRejectTwoOrMoreHanCharacters() throws Exception {
        LocaleContextHolder.setLocale(Locale.KOREAN);
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod("containsDisallowedHanScript", String.class);
        method.setAccessible(true);

        assertFalse((Boolean) method.invoke(service, "기록에 나온 내용이에요."));
        assertFalse((Boolean) method.invoke(service, "기록에 人 한 글자만."));
        assertTrue((Boolean) method.invoke(service, "中国어가 섮였어요."));
        assertFalse((Boolean) method.invoke(service, "  "));
        assertFalse((Boolean) method.invoke(service, (Object) null));
    }

    /**
     * language fallback은 RAG 의도·컨텍스트 유무에 따라 카탈로그 키를 고른다.
     */
    @Test
    void buildLanguageFallback_shouldPickCatalogByIntent() throws Exception {
        LocaleContextHolder.setLocale(Locale.KOREAN);
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "buildLanguageFallback",
                String.class,
                RagContext.class
        );
        method.setAccessible(true);

        final Class<?> ragContextClass = Class.forName(
                RagContext.class.getName());
        final var emptyCtor = ragContextClass.getDeclaredConstructor(
                RagIntent.class, List.class, String.class,
                Class.forName("io.nicheblog.dreamdiary.feature.ai.person.PersonFocus")
        );
        emptyCtor.setAccessible(true);

        final Object noContext = emptyCtor.newInstance(RagIntent.LOOKUP, List.of(), null, null);
        final Object synthesisCtx = emptyCtor.newInstance(RagIntent.SYNTHESIS, List.of(), "ctx", null);
        final Object lookupCtx = emptyCtor.newInstance(RagIntent.LOOKUP, List.of(), "ctx", null);

        final String noCtxMsg = (String) method.invoke(service, "질문", noContext);
        final String synthesisMsg = (String) method.invoke(service, "질문", synthesisCtx);
        final String lookupMsg = (String) method.invoke(service, "질문", lookupCtx);

        assertTrue(noCtxMsg.contains("답하기 어려워"));
        assertTrue(synthesisMsg.contains("통섭"));
        assertTrue(lookupMsg.contains("한국어"));
    }

}
