package io.nicheblog.dreamdiary.feature.chat.service;

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
import io.nicheblog.dreamdiary.feature.journal.embedding.entity.JournalEntryEmbeddingEntity;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.RagSearchResult;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.service.JournalEntityFocusService;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.type.JournalEntityRoleType;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.Mockito;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * ChatOrchestrator 프롬프트 계약 테스트.
 */
class ChatOrchestratorTest {

    /** 테스트 전용 가상 인물 A (태도/의미 질문 기본 픽스처). */
    private static final String FIXTURE_PERSON_A = "민수";
    private static final String FIXTURE_PERSON_A_TAG = "[엔서클]#김민수";

    /** 테스트 전용 가상 인물 B (등장/appearance 질문 픽스처). */
    private static final String FIXTURE_PERSON_B = "지연";
    private static final String FIXTURE_PERSON_B_TAG = "[엔서클]#박지연";
    private static final String FIXTURE_PERSON_B_CANONICAL = "박지연";
    private static final String FIXTURE_PERSON_B_FALSE_POSITIVE_TAG = "[유명인]#문지연";



    /**
     * 테스트용 ChatOrchestrator. RAG 의도·병합은 {@link RagSearchFacade}에 위임하므로 facade를 항상 주입한다.
     */
    private static ChatOrchestrator newService() {
        return newService(null);
    }

    /**
     * @param ollama LLM 2차 의도 분류 등에서 쓸 클라이언트 (null 허용)
     */
    private static PersonSynthesisHybridService newHybridService() {
        final RagSearchFacade ragSearchFacade = new RagSearchFacade(null, null);
        final PersonFocusResolver personFocusResolver = new PersonFocusResolver(null, null, ragSearchFacade);
        final PersonSnapshotService personSnapshotService = new PersonSnapshotService(personFocusResolver);
        return new PersonSynthesisHybridService(null, personSnapshotService, personFocusResolver);
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
        return new ChatOrchestrator(
                null,
                null,
                null,
                ollama,
                ragSearchFacade,
                ragContextService,
                personFocusResolver,
                personSnapshotService,
                personSynthesisHybridService,
                responseGuardService,
                systemPromptBuilder,
                intentPromptResolver,
                null
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
     * 통섭형 인물 질문은 역할 추정 억제 지시를 포함하고, 제거된 스캐폴드 블록을 참조하지 않아야 합니다.
     *
     * <p>PERSON_FOCUS·PERSON_MEANING_SCAFFOLD 블록은 convergence로 제거됨 — personFocus가 해결된
     * 질문은 Path C(SNAPSHOT)로 가고, 이 레거시 프롬프트가 쓰이는 경로에서는 블록이 존재할 수 없다.</p>
     */
    @Test
    void buildIntentPrompt_shouldConstrainPersonRoleInferenceForSynthesis() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "buildIntentPrompt",
                RagIntent.class,
                String.class
        );
        method.setAccessible(true);

        final String prompt = (String) method.invoke(service, RagIntent.SYNTHESIS, null);

        assertTrue(prompt.contains("업무 협업"));
        assertTrue(prompt.contains("현실 관계 지위는 기록에 직접 나온 표현이 있을 때만"));
        assertFalse(prompt.contains("PERSON_FOCUS"));
        assertFalse(prompt.contains("PERSON_MEANING_SCAFFOLD"));
    }

    /**
     * entity catalog 역할 축은 한국어 해석 라벨로 변환되어야 합니다.
     */
    @Test
    void formatPersonRoleAxis_shouldUseKoreanAxisLabel() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "formatPersonRoleAxis",
                JournalEntityRoleType.class,
                int.class
        );
        method.setAccessible(true);

        final String label = (String) method.invoke(service, JournalEntityRoleType.TENSION, 3);

        assertTrue(label.contains("긴장"));
        assertTrue(label.contains("(3)"));
    }

    /**
     * 인물 의미 질문에서는 조사 제거 후 이름 token 자체가 남아야 합니다.
     */
    @Test
    void extractPersonFocusTokens_shouldKeepNamedPersonToken() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod("extractPersonFocusTokens", String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        final List<String> tokens = (List<String>) method.invoke(
                service,
                "민수는 내 Dreamdiary 기록에서 어떤 의미로 등장해?"
        );

        assertEquals(List.of("민수"), tokens);
    }

    /**
     * 호칭(님)이 붙은 인물 표현은 태그 검색용 토큰에서 제거되어야 합니다.
     */
    @Test
    void extractPersonFocusTokens_shouldStripHonorificSuffix() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod("extractPersonFocusTokens", String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        final List<String> tokens = (List<String>) method.invoke(
                service,
                "나는 민수님을 어떻게 생각하고 있니?"
        );

        assertTrue(tokens.contains("민수"));
        assertFalse(tokens.contains("민수님"));
    }

    /**
     * Entity-backed person focus should keep the canonical label and repeated surface forms
     * in the merged token list so tag-only person-meaning retrieval can match alias tags.
     */
    @Test
    void mergePersonFocusTokens_shouldIncludeCanonicalLabelAndSurfaceForms() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "mergePersonFocusTokens",
                List.class,
                JournalEntityFocusService.PersonEntityFocusSummary.class
        );
        method.setAccessible(true);

        final JournalEntityFocusService.PersonEntityFocusSummary entitySummary =
                new JournalEntityFocusService.PersonEntityFocusSummary(
                        7,
                        "민수",
                        "민수",
                        List.of("민수"),
                        5,
                        3,
                        "2026-01-02",
                        "2026-05-29",
                        Map.of("DREAM", 2, "DIARY", 1),
                        Map.of(io.nicheblog.dreamdiary.feature.journal.entitycatalog.type.JournalEntityRoleType.COLLABORATION, 2),
                        Map.of("민수님", 3, "민수", 2),
                        List.of(101, 102, 103)
                );

        @SuppressWarnings("unchecked")
        final List<String> mergedTokens = (List<String>) method.invoke(
                service,
                List.of("민수"),
                entitySummary
        );

        assertEquals(List.of("민수", "민수님"), mergedTokens);
    }

    /**
     * 태그·역할 축 없이 빈 주제 분류만 있는 person-meaning 답변은 hollow로 판정해야 합니다.
     */
    @Test
    void isHollowPersonMeaningResponse_shouldDetectGenericBuckets() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "isHollowPersonMeaningResponse",
                String.class,
                RagContext.class,
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContext("민수");
        final String hollowResponse =
                "민수는 팀 관계와 전략적 행동 측면에서 자주 등장합니다.";

        final boolean hollow = (boolean) method.invoke(
                service,
                hollowResponse,
                ragContext,
                "민수는 내 기록에서 어떤 의미야?"
        );

        assertTrue(hollow);
    }

    /**
     * 스캐폴드 메타 필드 유출 응답은 degraded로 판정해야 합니다.
     */
    @Test
    void isHollowPersonMeaningResponse_shouldRejectScaffoldLeak() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "isHollowPersonMeaningResponse",
                String.class,
                RagContext.class,
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContext("민수");
        final String leakedResponse =
                "역할 축 roleaxesko: 팀 동료 반복 축 repeated_tags: #dreamdiary";

        final boolean hollow = (boolean) method.invoke(
                service,
                leakedResponse,
                ragContext,
                "민수는 내 기록에서 어떤 의미야?"
        );

        assertTrue(hollow);
    }

    /**
     * 스냅샷에 없는 dreamdiary 잡음 태그만 인용한 응답도 degraded로 봐야 합니다.
     */
    @Test
    void isHollowPersonMeaningResponse_shouldRejectDreamdiaryNoiseTag() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "isHollowPersonMeaningResponse",
                String.class,
                RagContext.class,
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContext("민수");
        final String noisyResponse = "민수는 #dreamdiary 태그와 관련되어 등장합니다.";

        final boolean hollow = (boolean) method.invoke(
                service,
                noisyResponse,
                ragContext,
                "민수는 내 기록에서 어떤 의미야?"
        );

        assertTrue(hollow);
    }

    /**
     * person-meaning 반복 축에는 인물 토큰이 포함된 태그만 남겨야 합니다.
     */
    @Test
    void filterPersonMeaningTags_shouldKeepOnlyPersonRelevantTags() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "filterPersonMeaningTags",
                List.class,
                Class.forName("io.nicheblog.dreamdiary.feature.ai.person.PersonFocus")
        );
        method.setAccessible(true);

        final Object personFocus = buildTestPersonFocus("민수");
        final List<String> tags = List.of(
                "[엔서클]#김민수",
                "[엔서클]#박지연",
                "#회사불만",
                "#dreamdiary"
        );

        @SuppressWarnings("unchecked")
        final List<String> filtered = (List<String>) method.invoke(service, tags, personFocus);

        assertEquals(List.of("[엔서클]#김민수"), filtered);
    }

    /**
     * person-meaning 스니펫은 embedding_text 메타라인과 HTML을 제거해야 합니다.
     */
    @Test
    void sanitizePersonMeaningSnippet_shouldStripMetadataAndHtml() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "sanitizePersonMeaningSnippet",
                RagSearchResult.class,
                Class.forName("io.nicheblog.dreamdiary.feature.ai.person.PersonFocus")
        );
        method.setAccessible(true);

        final JournalEntryEmbeddingEntity entity = JournalEntryEmbeddingEntity.builder()
                .journalEntryId(101)
                .embeddingText(
                        "유형: DIARY\n"
                                + "날짜: 2026-01-01\n"
                                + "핵심 태그: [엔서클]#김민수\n"
                                + "본문: <p>오늘 민수와 회의했다</p>"
                )
                .build();
        final RagSearchResult result = RagSearchResult.builder().entity(entity).build();
        final Object personFocus = buildTestPersonFocus("민수");

        final String snippet = (String) method.invoke(service, result, personFocus);

        assertFalse(snippet.contains("유형:"));
        assertFalse(snippet.contains("<p>"));
        assertTrue(snippet.contains("민수"));
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
     * person-meaning fallback은 인물 태그 외 연결 맥락 태그를 제외해야 합니다.
     */
    @Test
    void filterPersonMeaningLinkedContextTags_shouldExcludePersonFocusTags() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "filterPersonMeaningLinkedContextTags",
                List.class,
                Class.forName("io.nicheblog.dreamdiary.feature.ai.person.PersonFocus")
        );
        method.setAccessible(true);

        final Object personFocus = buildTestPersonFocus("민수");
        final List<String> tags = List.of(
                "[엔서클]#김민수",
                "[엔서클]#조직역동",
                "[엔서클]#김종순"
        );

        @SuppressWarnings("unchecked")
        final List<String> filtered = (List<String>) method.invoke(service, tags, personFocus);

        assertEquals(
                List.of("[엔서클]#조직역동", "[엔서클]#김종순"),
                filtered
        );
    }

    /**
     * 연결 맥락 태그 핵심어를 인용한 person-meaning 답은 hollow가 아니어야 합니다.
     */
    @Test
    void isHollowPersonMeaningResponse_shouldAcceptLinkedContextTagCitation() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "isHollowPersonMeaningResponse",
                String.class,
                RagContext.class,
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContextWithTaggedResults("민수");
        final String interpretiveResponse =
                "기록상 민수는 [엔서클]#조직역동 태그가 자주 같이 붙는 "
                        + "회고 말머리의 일기에서 조직 역동 맥락의 인물로 반복돼.";

        final boolean hollow = (boolean) method.invoke(
                service,
                interpretiveResponse,
                ragContext,
                "민수는 내 기록에서 어떤 의미야?"
        );

        assertFalse(hollow);
    }

    /**
     * 태그 핵심어 인용 검사는 # 이후 문자열도 허용해야 합니다.
     */
    @Test
    void citesPersonMeaningTagEvidence_shouldAcceptHashStem() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "citesPersonMeaningTagEvidence",
                String.class,
                Map.class
        );
        method.setAccessible(true);

        final boolean cited = (boolean) method.invoke(
                service,
                "민수는 조직역동 맥락에서 자주 등장합니다.",
                Map.of("[엔서클]#조직역동", 3)
        );

        assertTrue(cited);
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

        final Object ragContext = buildTestRagContextWithTaggedResults("민수");
        final String fallback = (String) method.invoke(service, ragContext);

        assertTrue(fallback.contains("연결 맥락"));
        assertTrue(fallback.contains("조직역동"));
        assertTrue(fallback.contains("챕터 말머리"));
        assertTrue(fallback.contains("회고"));
        assertFalse(fallback.toLowerCase().contains("entity catalog"));
    }

    /**
     * TAG 매칭 source는 ENTITY source보다 높은 person focus 우선순위를 가져야 합니다.
     */
    @Test
    void resolvePersonFocusMatchPriority_shouldPreferTagOverEntity() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "resolvePersonFocusMatchPriority",
                RagSearchResult.class,
                Class.forName("io.nicheblog.dreamdiary.feature.ai.person.PersonFocus")
        );
        method.setAccessible(true);

        final Object personFocus = buildTestPersonFocus("민수");
        final RagSearchResult tagResult = RagSearchResult.builder()
                .matchType(RagSearchResult.MATCH_TYPE_TAG)
                .build();
        final RagSearchResult entityResult = RagSearchResult.builder()
                .matchType(RagSearchResult.MATCH_TYPE_ENTITY)
                .build();

        final int tagPriority = (int) method.invoke(service, tagResult, personFocus);
        final int entityPriority = (int) method.invoke(service, entityResult, personFocus);

        assertTrue(tagPriority > entityPriority);
    }

    /**
     * person-meaning 집계는 person 태그가 없으면 본문 언급 source로 대체하지 않아야 합니다.
     */
    @Test
    void resolvePersonFocusedResults_shouldNotFallbackToBodyMentionWithoutPersonTag() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "resolvePersonFocusedResults",
                List.class,
                Class.forName("io.nicheblog.dreamdiary.feature.ai.person.PersonFocus")
        );
        method.setAccessible(true);

        final Object personFocus = buildTestPersonFocus("민수");
        final JournalEntryEmbeddingEntity entity = JournalEntryEmbeddingEntity.builder()
                .journalEntryId(101)
                .embeddingText("본문: 오늘 민수와 회의했다")
                .embeddingPayloadJson("{\"tags\":\"[일상]#회의\"}")
                .build();
        final RagSearchResult bodyMentionOnly = RagSearchResult.builder()
                .entity(entity)
                .matchType(RagSearchResult.MATCH_TYPE_KEYWORD)
                .build();

        @SuppressWarnings("unchecked")
        final List<RagSearchResult> focused = (List<RagSearchResult>) method.invoke(
                service,
                List.of(bodyMentionOnly),
                personFocus
        );

        assertTrue(focused.isEmpty());
    }

    /**
     * person 태그가 없을 때 근거 스니펫 인용은 guard 증거로 인정해야 합니다.
     */
    @Test
    void citesPersonMeaningSnippetEvidence_shouldDetectSnippetOverlap() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "citesPersonMeaningSnippetEvidence",
                String.class,
                List.class
        );
        method.setAccessible(true);

        final boolean cited = (boolean) method.invoke(
                service,
                "기록을 보면 오늘 민수와 회의했다.",
                List.of("오늘 민수와 회의했다")
        );

        assertTrue(cited);
    }


    private static String fixturePersonTagFor(final String target) {
        if (FIXTURE_PERSON_B.equals(target)) {
            return FIXTURE_PERSON_B_TAG;
        }
        return FIXTURE_PERSON_A_TAG;
    }

    private static Object buildTestPersonFocus(final String target) throws Exception {
        final Class<?> personFocusClass = Class.forName("io.nicheblog.dreamdiary.feature.ai.person.PersonFocus");
        final var personFocusCtor = personFocusClass.getDeclaredConstructor(
                String.class,
                List.class,
                int.class,
                JournalEntityFocusService.PersonEntityFocusSummary.class
        );
        personFocusCtor.setAccessible(true);
        return personFocusCtor.newInstance(target, List.of(target), 1, null);
    }

    private static Object buildTestRagContext(final String target) throws Exception {
        final Class<?> personFocusClass = Class.forName("io.nicheblog.dreamdiary.feature.ai.person.PersonFocus");
        final Class<?> ragContextClass = RagContext.class;

        final var personFocusCtor = personFocusClass.getDeclaredConstructor(
                String.class,
                List.class,
                int.class,
                JournalEntityFocusService.PersonEntityFocusSummary.class
        );
        personFocusCtor.setAccessible(true);
        final Object personFocus = personFocusCtor.newInstance(target, List.of(target), 1, null);

        final var ragContextCtor = ragContextClass.getDeclaredConstructor(
                RagIntent.class,
                List.class,
                String.class,
                personFocusClass
        );
        ragContextCtor.setAccessible(true);
        return ragContextCtor.newInstance(RagIntent.SYNTHESIS, List.of(), "ctx", personFocus);
    }

    private static Object buildTestRagContextWithTaggedResults(final String target) throws Exception {
        final Class<?> personFocusClass = Class.forName("io.nicheblog.dreamdiary.feature.ai.person.PersonFocus");
        final Class<?> ragContextClass = RagContext.class;

        final var personFocusCtor = personFocusClass.getDeclaredConstructor(
                String.class,
                List.class,
                int.class,
                JournalEntityFocusService.PersonEntityFocusSummary.class
        );
        personFocusCtor.setAccessible(true);
        final Object personFocus = personFocusCtor.newInstance(target, List.of(target), 1, null);

        final String personTag = fixturePersonTagFor(target);
        final JournalEntryEmbeddingEntity entity = JournalEntryEmbeddingEntity.builder()
                .journalEntryId(101)
                .contentKind("DIARY")
                .embeddingText("본문: " + target + "님과 회의했다")
                .embeddingPayloadJson(
                        "{\"tags\":\"" + personTag + " [엔서클]#조직역동\","
                                + "\"journalChapterPrefixName\":\"회고\"}"
                )
                .build();
        final RagSearchResult taggedResult = RagSearchResult.builder()
                .entity(entity)
                .matchType(RagSearchResult.MATCH_TYPE_TAG)
                .score(5.0D)
                .build();

        final var ragContextCtor = ragContextClass.getDeclaredConstructor(
                RagIntent.class,
                List.class,
                String.class,
                personFocusClass
        );
        ragContextCtor.setAccessible(true);
        return ragContextCtor.newInstance(RagIntent.SYNTHESIS, List.of(taggedResult), "ctx", personFocus);
    }

    private static Object buildTestRagContextWithManyTaggedResults(final String target, final int count) throws Exception {
        final Class<?> personFocusClass = Class.forName("io.nicheblog.dreamdiary.feature.ai.person.PersonFocus");
        final Class<?> ragContextClass = RagContext.class;

        final var personFocusCtor = personFocusClass.getDeclaredConstructor(
                String.class,
                List.class,
                int.class,
                JournalEntityFocusService.PersonEntityFocusSummary.class
        );
        personFocusCtor.setAccessible(true);
        final Object personFocus = personFocusCtor.newInstance(target, List.of(target), count, null);

        final List<RagSearchResult> results = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            final JournalEntryEmbeddingEntity entity = JournalEntryEmbeddingEntity.builder()
                    .journalEntryId(101 + i)
                    .contentKind("DIARY")
                    .embeddingText("본문: 장면" + i + " " + target + "님과 회의했다")
                    .embeddingPayloadJson(
                            "{\"tags\":\"" + fixturePersonTagFor(target) + " [엔서클]#조직역동\","
                                    + "\"journalChapterPrefixName\":\"회고\"}"
                    )
                    .build();
            results.add(RagSearchResult.builder()
                    .entity(entity)
                    .matchType(RagSearchResult.MATCH_TYPE_TAG)
                    .score(5.0D + i)
                    .build());
        }

        final var ragContextCtor = ragContextClass.getDeclaredConstructor(
                RagIntent.class,
                List.class,
                String.class,
                personFocusClass
        );
        ragContextCtor.setAccessible(true);
        return ragContextCtor.newInstance(RagIntent.SYNTHESIS, results, "ctx", personFocus);
    }

    /**
     * "어떻게 생각" 류 질문은 통섭형(SYNTHESIS)으로 분류해야 합니다.
     */
    @Test
    void detectRagIntent_shouldTreatAttitudeQuestionAsSynthesis() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod("detectRagIntent", String.class);
        method.setAccessible(true);

        final RagIntent intent = (RagIntent) method.invoke(
                service,
                "나는 민수님을 어떻게 생각하고 있니?"
        );

        assertEquals(RagIntent.SYNTHESIS, intent);
    }

    /**
     * person-meaning 힌트는 태도/감정 질문도 인식해야 합니다.
     */
    @Test
    void isPersonMeaningQuery_shouldRecognizeHowYouThinkQuestion() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod("isPersonMeaningQuery", String.class);
        method.setAccessible(true);

        final boolean personMeaning = (boolean) method.invoke(
                service,
                "나는 민수님을 어떻게 생각하고 있니?"
        );

        assertTrue(personMeaning);
    }

    /**
     * '느끼고'/'어떻게 느끼' 표현의 태도 질문도 SYNTHESIS로 수렴해야 합니다(③ 라우팅 수렴).
     *
     * <p>이 표현이 SYNTHESIS로 분류돼야 Path C(rich-trust)로 라우팅되고, 레거시 LOOKUP 저하 경로로
     * 새지 않는다.</p>
     */
    @Test
    void detectRagIntent_shouldTreatFeelVerbAttitudeAsSynthesis() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod("detectRagIntent", String.class);
        method.setAccessible(true);

        final RagIntent intent = (RagIntent) method.invoke(
                service,
                "나는 민수이를 어떻게 느끼고 있지?"
        );

        assertEquals(RagIntent.SYNTHESIS, intent);
    }

    /**
     * 태도 질문 검색 폭은 tag-only·merged 경로 공통으로 확대 폭(50)을 써야 합니다(F3 정렬).
     *
     * <p>일반 SYNTHESIS 질문은 기본 폭(25)을 유지한다. merged 폴백 경로가 queryText를 넘기지 않아
     * 태도 질문이 기본 폭으로 좁아지던 불일치를 고정하는 계약.</p>
     */
    @Test
    void resolveRagTopK_shouldUseStanceBudgetForAttitudeQuery() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "resolveRagTopK",
                RagIntent.class,
                String.class
        );
        method.setAccessible(true);

        final int stanceTopK = (int) method.invoke(service, RagIntent.SYNTHESIS, "나는 민수님을 어떻게 생각하고 있니?");
        final int synthesisTopK = (int) method.invoke(service, RagIntent.SYNTHESIS, "내 기록의 반복 패턴을 해석해줘");

        assertEquals(50, stanceTopK);
        assertEquals(25, synthesisTopK);
    }

    /**
     * '느끼고' 태도 질문도 person-meaning으로 인식해 Path C 조건(SYNTHESIS+meaning)을 만족해야 합니다(③ 라우팅 수렴).
     */
    @Test
    void isPersonMeaningQuery_shouldRecognizeFeelVerbAttitude() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod("isPersonMeaningQuery", String.class);
        method.setAccessible(true);

        final boolean personMeaning = (boolean) method.invoke(
                service,
                "나는 민수이를 어떻게 느끼고 있지?"
        );

        assertTrue(personMeaning);
    }

    /**
     * LOOKUP 인물 질문 프롬프트는 조직 일반론 억제와 인덱스 인용 금지를 포함해야 합니다.
     */
    @Test
    void buildIntentPrompt_shouldConstrainLookupPersonQueries() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "buildIntentPrompt",
                RagIntent.class,
                String.class
        );
        method.setAccessible(true);

        final String prompt = (String) method.invoke(
                service,
                RagIntent.LOOKUP,
                "민수님은 뭐 했어?"
        );

        assertTrue(prompt.contains("조직"));
        assertTrue(prompt.contains("[1]"));
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
     * 대화 등장 질문은 appearance 전용 intent 프롬프트를 써야 합니다.
     */
    @Test
    void buildIntentPrompt_shouldUseAppearanceBranchForDialogueQuestion() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "buildIntentPrompt",
                RagIntent.class,
                String.class
        );
        method.setAccessible(true);

        final String prompt = (String) method.invoke(
                service,
                RagIntent.SYNTHESIS,
                "내 대화에서 지연님은 어떤 느낌으로 등장하고 있니"
        );

        assertTrue(prompt.contains("등장"));
        assertTrue(prompt.contains("추론하자면"));
        assertFalse(prompt.contains("PERSON_MEANING_SCAFFOLD"));
        assertFalse(prompt.contains("PERSON_STANCE_SCAFFOLD"));
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

        final Object ragContext = buildTestRagContextWithTaggedResults("지연");
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
     * SYNTHESIS 태도 질문 intent 프롬프트는 rich-trust(자유 산문·2인칭 비춤·반환각만 금지)여야 합니다.
     *
     * <p>이 분기는 personFocus 미해결(기록에 단서 없는 인물) 레거시 경로에서만 쓰이며,
     * 예전 PERSON_STANCE_SCAFFOLD 골격·조언/톤 금지 레짐은 재도입하지 않는다.</p>
     */
    @Test
    void buildIntentPrompt_shouldUseRichTrustProseForAttitudeQuestion() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "buildIntentPrompt",
                RagIntent.class,
                String.class
        );
        method.setAccessible(true);

        final String prompt = (String) method.invoke(
                service,
                RagIntent.SYNTHESIS,
                "나는 민수님을 어떻게 생각하고 있니?"
        );

        assertTrue(prompt.contains("네가 기록에 남긴 바로는"));
        assertTrue(prompt.contains("형식은 자유"));
        assertTrue(prompt.contains("기록만으로는 확실치 않다"));
        assertFalse(prompt.contains("PERSON_STANCE_SCAFFOLD"));
        assertFalse(prompt.contains("고려할 수 있"));
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

        final Object ragContext = buildTestRagContextWithTaggedResults("민수");

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

        final Object ragContext = buildTestRagContextWithTaggedResults("민수");
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

        final Object ragContext = buildTestRagContextWithTaggedResults("민수");
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

        final Object ragContext = buildTestRagContextWithTaggedResults("민수");
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

        final Object ragContext = buildTestRagContext("민수");
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
     * 사용자 표시용 태그 문자열은 [엔서클] 접두를 제거해야 합니다.
     */
    @Test
    void formatDisplayTag_shouldStripMetaPrefix() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod("formatDisplayTag", String.class);
        method.setAccessible(true);

        final String display = (String) method.invoke(service, "[엔서클]#조직역동");

        assertEquals("#조직역동", display);
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

        final Object ragContext = buildTestRagContextWithTaggedResults("지연");
        final String response = (String) method.invoke(service, ragContext);

        assertTrue(response.contains("(1) 등장 느낌"));
        assertTrue(response.contains("(2) 반복 맥락"));
        assertTrue(response.contains("(3) 함께 묶인 축"));
        assertTrue(response.contains("(4) 확정 불가"));
        assertFalse(response.contains("[엔서클]"));
    }

    /**
     * appearance 질문 토큰 추출 시 범위어(대화)보다 인물명(지연)을 남겨야 합니다.
     */
    @Test
    void extractPersonFocusTokens_shouldPreferPersonNameOverDialogueScopeWord() throws Exception {
        final ChatOrchestrator service = newService();
        final Method extractMethod = ChatOrchestrator.class.getDeclaredMethod("extractPersonFocusTokens", String.class);
        extractMethod.setAccessible(true);

        @SuppressWarnings("unchecked")
        final List<String> tokens = (List<String>) extractMethod.invoke(
                service,
                "내 대화에서 지연님은 어떤 느낌으로 등장하고 있니"
        );

        assertTrue(tokens.contains("지연"));
        assertFalse(tokens.contains("대화"));
    }

    /**
     * primary person token은 질문의 님 호칭 토큰을 우선해야 합니다.
     */
    @Test
    void selectPrimaryPersonTokenFromQuery_shouldPreferHonorificPersonToken() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "selectPrimaryPersonTokenFromQuery",
                String.class,
                List.class
        );
        method.setAccessible(true);

        final String primary = (String) method.invoke(
                service,
                "내 대화에서 지연님은 어떤 느낌으로 등장하고 있니",
                List.of("지연", "Dreamdiary")
        );

        assertEquals("지연", primary);
    }

    /**
     * dominant stem이 있으면 짧은 토큰 오매칭 태그(#문지연)를 제외해야 합니다.
     */
    @Test
    void isPersonRelevantTag_shouldUseDominantStemForShortPersonToken() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "isPersonRelevantTag",
                String.class,
                Class.forName("io.nicheblog.dreamdiary.feature.ai.person.PersonFocus"),
                String.class
        );
        method.setAccessible(true);

        final Object personFocus = buildTestPersonFocus("지연");
        final boolean matchesPrimaryTag = (boolean) method.invoke(
                service,
                "[엔서클]#박지연",
                personFocus,
                "박지연"
        );
        final boolean matchesFalsePositiveTag = (boolean) method.invoke(
                service,
                "[유명인]#문지연",
                personFocus,
                "박지연"
        );

        assertTrue(matchesPrimaryTag);
        assertFalse(matchesFalsePositiveTag);
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
     * person focus 표시 이름은 entity catalog canonical label을 우선해야 합니다.
     */
    @Test
    void resolvePersonFocusTarget_shouldPreferEntitySummaryCanonicalLabel() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "resolvePersonFocusTarget",
                Class.forName("io.nicheblog.dreamdiary.feature.ai.person.PersonFocus")
        );
        method.setAccessible(true);

        final JournalEntityFocusService.PersonEntityFocusSummary entitySummary =
                new JournalEntityFocusService.PersonEntityFocusSummary(
                        12,
                        "박지연",
                        "박지연",
                        List.of("지연"),
                        0,
                        0,
                        null,
                        null,
                        Map.of(),
                        Map.of(),
                        Map.of(),
                        List.of()
                );
        final Class<?> personFocusClass = Class.forName(
                "io.nicheblog.dreamdiary.feature.ai.person.PersonFocus"
        );
        final var personFocusCtor = personFocusClass.getDeclaredConstructor(
                String.class,
                List.class,
                int.class,
                JournalEntityFocusService.PersonEntityFocusSummary.class
        );
        personFocusCtor.setAccessible(true);
        final Object personFocus = personFocusCtor.newInstance(
                "지연",
                List.of("지연"),
                5,
                entitySummary
        );

        final String target = (String) method.invoke(service, personFocus);

        assertEquals("박지연", target);
    }

    /**
     * tag-only person-meaning 결과는 TAG match type만 포함해야 합니다.
     */
    @Test
    void isTagOnlyPersonMeaningResults_shouldBeTrueForTagMatchesOnly() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "isTagOnlyPersonMeaningResults",
                List.class
        );
        method.setAccessible(true);

        final RagSearchResult tagResult = RagSearchResult.builder()
                .matchType(RagSearchResult.MATCH_TYPE_TAG)
                .score(1.0D)
                .build();
        final RagSearchResult keywordResult = RagSearchResult.builder()
                .matchType(RagSearchResult.MATCH_TYPE_KEYWORD)
                .score(1.0D)
                .build();

        final boolean tagOnly = (boolean) method.invoke(service, List.of(tagResult));
        final boolean mixed = (boolean) method.invoke(service, List.of(tagResult, keywordResult));

        assertTrue(tagOnly);
        assertFalse(mixed);
    }

    /**
     * hybrid 시스템 프롬프트는 SNAPSHOT 블록과 표시용 태그를 포함해야 합니다.
     */
    @Test
    void buildPersonSynthesisHybridSystemPrompt_shouldIncludeSnapshotWithDisplayTags() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "buildPersonSynthesisHybridSystemPrompt",
                RagContext.class,
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContextWithTaggedResults("민수");
        final String prompt = (String) method.invoke(
                service,
                ragContext,
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

        final Object ragContext = buildTestRagContextWithTaggedResults("민수");
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

        final Object ragContext = buildTestRagContextWithTaggedResults("민수");
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

        final Object ragContext = buildTestRagContextWithTaggedResults("민수");
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
     * '~에 대해 뭘 말해' 류 인물 질문은 person-meaning 경로로 인식해야 합니다.
     */
    @Test
    void isPersonMeaningQuery_shouldRecognizePersonAboutQuestion() throws Exception {
        final ChatOrchestrator service = newService();
        final Method meaningMethod = ChatOrchestrator.class.getDeclaredMethod("isPersonMeaningQuery", String.class);
        meaningMethod.setAccessible(true);

        final boolean personMeaning = (boolean) meaningMethod.invoke(
                service,
                "민수님에 대해 뭘 말해줘 수 있니?"
        );

        assertTrue(personMeaning);
    }

    /**
     * 인물 about 질문은 SYNTHESIS intent로 분류해야 합니다.
     */
    @Test
    void detectRagIntent_shouldTreatPersonAboutQuestionAsSynthesis() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod("detectRagIntent", String.class);
        method.setAccessible(true);

        final RagIntent intent = (RagIntent) method.invoke(
                service,
                "민수님에 대해 뭘 말해줘 수 있니?"
        );

        assertEquals(RagIntent.SYNTHESIS, intent);
    }
    /**
     * person-about LOOKUP 힌트는 뭘/무엇을 말해·알려 표현을 인식해야 합니다.
     */
    @Test
    void isPersonAboutLookupQuery_shouldRecognizeFixedLookupHints() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod("isPersonAboutLookupQuery", String.class);
        method.setAccessible(true);

        assertTrue((boolean) method.invoke(service, "민수님에 대해 무엇을 말해줄 수 있니?"));
        assertTrue((boolean) method.invoke(service, "민수님에 대해 뭘 알려줘"));
        assertFalse((boolean) method.invoke(service, "오늘 꿈 해석해줘"));
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
     * RULE_PRIMARY fallback 근거 장면은 hybrid와 같이 최대 3건을 | 로 이어야 합니다.
     */
    @Test
    void appendRulePrimaryEvidenceSection_shouldIncludeUpToThreeSnippets() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "appendRulePrimaryEvidenceSection",
                StringBuilder.class,
                List.class
        );
        method.setAccessible(true);

        final StringBuilder sb = new StringBuilder();
        method.invoke(service, sb, List.of("장면A", "장면B", "장면C", "장면D"));

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
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "compactRulePrimaryEvidenceSnippet",
                String.class
        );
        method.setAccessible(true);

        final String compact = (String) method.invoke(
                service,
                "... \"예상과 달랐다고\" \"말했다\" 지연님: \"오전 회의\" 나: \"자료 확인\" 민수님은 옆에서 지켜봤고."
        );

        assertFalse(compact.contains("\""));
        assertFalse(compact.contains("지연님:"));
        assertTrue(compact.contains("민수"));
    }
    /**
     * hybrid 폴백 시 재시도 가드 사유는 metadataJson.retryGuardDetail에 저장해야 합니다.
     */
    @Test
    void buildRagMetadataJson_shouldIncludeRetryGuardDetail() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "buildRagMetadataJson",
                RagContext.class,
                String.class,
                String.class,
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContextWithTaggedResults("민수");
        final String json = (String) method.invoke(
                service,
                ragContext,
                "RULE_PRIMARY",
                "person_stance_generic_bucket",
                "person_stance_too_short"
        );

        assertNotNull(json);
        assertTrue(json.contains("\"guardDetail\":\"person_stance_generic_bucket\""));
        assertTrue(json.contains("\"retryGuardDetail\":\"person_stance_too_short\""));
    }

    /**
     * 태도 결정론 폴백(rich-trust)은 4섹션 태그 덤프 대신 2인칭 산문 근거 노트를 만들어야 합니다.
     *
     * <p>해석 리드로 연결 맥락 태그를 근거로 인용하고, 기록에 없는 건 단정하지 않는다는 문구를 남기되,
     * 예전 (1)~(4) 섹션 헤더 형식은 재도입하지 않는다.</p>
     */
    @Test
    void buildPersonStanceDeterministicFallback_shouldWriteGroundedSecondPersonProse() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "buildPersonStanceDeterministicFallback",
                RagContext.class,
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContextWithTaggedResults("민수");
        final String fallback = (String) method.invoke(
                service,
                ragContext,
                "나는 민수님을 어떻게 생각하고 있니?"
        );

        assertTrue(fallback.contains("네가 기록에 남긴"));
        assertTrue(fallback.contains("조직역동"));
        assertTrue(fallback.contains("단정하기 어려워"));
        assertFalse(fallback.contains("(1) 내 태도·정서"));
        assertFalse(fallback.contains("(4) 확정 불가"));
    }



    /**
     * 태도 질문 SNAPSHOT은 근거 장면을 더 많이·고르게 샘플링해야 합니다.
     */
    @Test
    void buildPersonMeaningSnapshot_stanceQuery_shouldSpreadRichEvidence() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "buildPersonMeaningSnapshot",
                RagContext.class,
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContextWithManyTaggedResults("민수", 12);
        final Object snapshot = method.invoke(
                service,
                ragContext,
                "나는 민수님을 어떻게 생각하고 있니?"
        );

        final Method evidenceMethod = snapshot.getClass().getDeclaredMethod("evidenceSnippets");
        @SuppressWarnings("unchecked")
        final List<String> evidenceSnippets = (List<String>) evidenceMethod.invoke(snapshot);

        assertEquals(12, evidenceSnippets.size());
        assertTrue(evidenceSnippets.stream().anyMatch(snippet -> snippet.contains("장면0")));
        assertTrue(evidenceSnippets.stream().anyMatch(snippet -> snippet.contains("장면11")));
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
     * 풍부 신뢰 게이트: 기록 근거 없는 빈 조직 버킷 나열 태도 답변은 거부해야 합니다.
     */
    @Test
    void isDegradedPersonStanceRichResponse_shouldRejectGenericBucket() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "isDegradedPersonStanceRichResponse",
                String.class
        );
        method.setAccessible(true);

        final boolean degraded = (boolean) method.invoke(
                service,
                "민수님은 조직 내에서 중요한 역할을 하며 업무 협업에 기여하는 것으로 보입니다."
        );

        assertTrue(degraded);
    }

    /**
     * 풍부 신뢰 게이트: 기록 근거가 담긴 긴 산문 태도 답변은 통과해야 합니다.
     */
    @Test
    void isDegradedPersonStanceRichResponse_shouldAcceptGroundedProse() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "isDegradedPersonStanceRichResponse",
                String.class
        );
        method.setAccessible(true);

        final boolean degraded = (boolean) method.invoke(
                service,
                "네가 기록에 남긴 바로는, #조직역동 맥락에서 민수와 부딪힐 때 미묘한 기싸움을 반복해서 느낀 것 같아. "
                        + "화면을 기웃거리는 장면을 여러 번 적어 두었고, 그때마다 경계심이 배어 있어."
        );

        assertFalse(degraded);
    }

    /**
     * 풍부 신뢰 게이트 사유 코드: 빈 버킷은 person_stance_generic_bucket 로 표기해야 합니다.
     */
    @Test
    void describePersonStanceRichGuardFailure_shouldReturnGenericBucketCode() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod(
                "describePersonStanceRichGuardFailure",
                String.class
        );
        method.setAccessible(true);

        final String code = (String) method.invoke(
                service,
                "민수님은 조직 내에서 중요한 역할을 하며 업무 협업에 기여하는 것으로 보이며 전략적 존재감이 있습니다."
        );

        assertEquals("person_stance_generic_bucket", code);
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

    /**
     * SUMMARY+SYNTHESIS 모호 질문은 LLM 2차 레이블을 우선한다.
     */
    @Test
    void detectRagIntent_shouldPreferLlmWhenAmbiguous() throws Exception {
        final OllamaClient ollama = Mockito.mock(OllamaClient.class);
        when(ollama.chat(anyString(), anyString())).thenReturn("SUMMARY");
        final ChatOrchestrator service = newService(ollama);
        final Method method = ChatOrchestrator.class.getDeclaredMethod("detectRagIntent", String.class);
        method.setAccessible(true);

        final RagIntent intent = (RagIntent) method.invoke(service, "의미를 정리해줘");

        assertEquals(RagIntent.SUMMARY, intent);
    }

    /**
     * LLM 실패 시 휴리스틱(SYNTHESIS 우선)으로 돌아간다.
     */
    @Test
    void detectRagIntent_shouldFallbackToHeuristicWhenLlmFails() throws Exception {
        final OllamaClient ollama = Mockito.mock(OllamaClient.class);
        when(ollama.chat(anyString(), anyString())).thenThrow(new IllegalStateException("ollama down"));
        final ChatOrchestrator service = newService(ollama);
        final Method method = ChatOrchestrator.class.getDeclaredMethod("detectRagIntent", String.class);
        method.setAccessible(true);

        final RagIntent intent = (RagIntent) method.invoke(service, "의미를 정리해줘");

        assertEquals(RagIntent.SYNTHESIS, intent);
    }

    /**
     * LLM 응답에서 의도 레이블을 추출한다.
     */
    @Test
    void parseRagIntentLabel_shouldExtractFirstKnownLabel() throws Exception {
        final ChatOrchestrator service = newService();
        final Method method = ChatOrchestrator.class.getDeclaredMethod("parseRagIntentLabel", String.class);
        method.setAccessible(true);

        assertEquals(RagIntent.LOOKUP, method.invoke(service, "LOOKUP"));
        assertEquals(RagIntent.SUMMARY, method.invoke(service, "의도: SUMMARY\n설명 생략"));
        assertEquals(RagIntent.SYNTHESIS, method.invoke(service, "I choose SYNTHESIS for this."));
        assertEquals(null, method.invoke(service, "unknown"));
    }

}
