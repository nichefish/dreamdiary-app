package io.nicheblog.dreamdiary.feature.chat.service;

import io.nicheblog.dreamdiary.feature.chat.model.RagIntent;
import io.nicheblog.dreamdiary.feature.journal.embedding.entity.JournalEntryEmbeddingEntity;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.RagSearchResult;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.service.JournalEntityFocusService;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.type.JournalEntityRoleType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ChatAIService 프롬프트 계약 테스트.
 */
class ChatAIServiceTest {

    /**
     * 통섭형 인물 질문은 PERSON_FOCUS 축과 역할 추정 억제 지시를 함께 포함해야 합니다.
     */
    @Test
    void buildIntentPrompt_shouldConstrainPersonRoleInferenceForSynthesis() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "buildIntentPrompt",
                RagIntent.class,
                String.class
        );
        method.setAccessible(true);

        final String prompt = (String) method.invoke(service, RagIntent.SYNTHESIS, null);

        assertTrue(prompt.contains("PERSON_FOCUS"));
        assertTrue(prompt.contains("PERSON_MEANING_SCAFFOLD"));
        assertTrue(prompt.contains("\uC5C5\uBB34 \uD611\uC5C5"));
    }

    /**
     * entity catalog 역할 축은 한국어 해석 라벨로 변환되어야 합니다.
     */
    @Test
    void formatPersonRoleAxis_shouldUseKoreanAxisLabel() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "formatPersonRoleAxis",
                JournalEntityRoleType.class,
                int.class
        );
        method.setAccessible(true);

        final String label = (String) method.invoke(service, JournalEntityRoleType.TENSION, 3);

        assertTrue(label.contains("\uAE34\uC7A5"));
        assertTrue(label.contains("(3)"));
    }

    /**
     * 인물 의미 질문에서는 조사 제거 후 이름 token 자체가 남아야 합니다.
     */
    @Test
    void extractPersonFocusTokens_shouldKeepNamedPersonToken() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod("extractPersonFocusTokens", String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        final List<String> tokens = (List<String>) method.invoke(
                service,
                "\uC6D0\uBE48\uC740 \uB0B4 Dreamdiary \uAE30\uB85D\uC5D0\uC11C \uC5B4\uB5A4 \uC758\uBBF8\uB85C \uB4F1\uC7A5\uD574?"
        );

        assertEquals(List.of("\uC6D0\uBE48"), tokens);
    }

    /**
     * 호칭(님)이 붙은 인물 표현은 태그 검색용 토큰에서 제거되어야 합니다.
     */
    @Test
    void extractPersonFocusTokens_shouldStripHonorificSuffix() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod("extractPersonFocusTokens", String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        final List<String> tokens = (List<String>) method.invoke(
                service,
                "\uB098\uB294 \uC6D0\uBE48\uB2D8\uC744 \uC5B4\uB5BB\uAC8C \uC0DD\uAC01\uD558\uACE0 \uC788\uB2C8?"
        );

        assertTrue(tokens.contains("\uC6D0\uBE48"));
        assertFalse(tokens.contains("\uC6D0\uBE48\uB2D8"));
    }

    /**
     * Entity-backed person focus should keep the canonical label and repeated surface forms
     * in the merged token list so tag-only person-meaning retrieval can match alias tags.
     */
    @Test
    void mergePersonFocusTokens_shouldIncludeCanonicalLabelAndSurfaceForms() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "mergePersonFocusTokens",
                List.class,
                JournalEntityFocusService.PersonEntityFocusSummary.class
        );
        method.setAccessible(true);

        final JournalEntityFocusService.PersonEntityFocusSummary entitySummary =
                new JournalEntityFocusService.PersonEntityFocusSummary(
                        7,
                        "\uC6D0\uBE48",
                        "\uC6D0\uBE48",
                        List.of("\uC6D0\uBE48"),
                        5,
                        3,
                        "2026-01-02",
                        "2026-05-29",
                        Map.of("DREAM", 2, "DIARY", 1),
                        Map.of(io.nicheblog.dreamdiary.feature.journal.entitycatalog.type.JournalEntityRoleType.COLLABORATION, 2),
                        Map.of("\uC6D0\uBE48\uB2D8", 3, "\uC6D0\uBE48", 2),
                        List.of(101, 102, 103)
                );

        @SuppressWarnings("unchecked")
        final List<String> mergedTokens = (List<String>) method.invoke(
                service,
                List.of("\uC6D0\uBE48"),
                entitySummary
        );

        assertEquals(List.of("\uC6D0\uBE48", "\uC6D0\uBE48\uB2D8"), mergedTokens);
    }

    /**
     * 태그·역할 축 없이 빈 주제 분류만 있는 person-meaning 답변은 hollow로 판정해야 합니다.
     */
    @Test
    void isHollowPersonMeaningResponse_shouldDetectGenericBuckets() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "isHollowPersonMeaningResponse",
                String.class,
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext")
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContext("\uC6D0\uBE48");
        final String hollowResponse =
                "\uC6D0\uBE48\uC740 \uD300 \uAD00\uACC4\uC640 \uC804\uB7B5\uC801 \uD589\uB3D9 \uCE21\uBA74\uC5D0\uC11C \uC790\uC8FC \uB4F1\uC7A5\uD569\uB2C8\uB2E4.";

        final boolean hollow = (boolean) method.invoke(service, hollowResponse, ragContext);

        assertTrue(hollow);
    }

    /**
     * 스캐폴드 메타 필드 유출 응답은 degraded로 판정해야 합니다.
     */
    @Test
    void isHollowPersonMeaningResponse_shouldRejectScaffoldLeak() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "isHollowPersonMeaningResponse",
                String.class,
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext")
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContext("\uC6D0\uBE48");
        final String leakedResponse =
                "\uC5ED\uD560 \uCD95 roleaxesko: \uD300 \uB3D9\uB8CC \uBC18\uBCF5 \uCD95 repeated_tags: #dreamdiary";

        final boolean hollow = (boolean) method.invoke(service, leakedResponse, ragContext);

        assertTrue(hollow);
    }

    /**
     * 스냅샷에 없는 dreamdiary 잡음 태그만 인용한 응답도 degraded로 봐야 합니다.
     */
    @Test
    void isHollowPersonMeaningResponse_shouldRejectDreamdiaryNoiseTag() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "isHollowPersonMeaningResponse",
                String.class,
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext")
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContext("\uC6D0\uBE48");
        final String noisyResponse = "\uC6D0\uBE48\uC740 #dreamdiary \uD0DC\uADF8\uC640 \uAD00\uB828\uB418\uC5B4 \uB4F1\uC7A5\uD569\uB2C8\uB2E4.";

        final boolean hollow = (boolean) method.invoke(service, noisyResponse, ragContext);

        assertTrue(hollow);
    }

    /**
     * person-meaning 반복 축에는 인물 토큰이 포함된 태그만 남겨야 합니다.
     */
    @Test
    void filterPersonMeaningTags_shouldKeepOnlyPersonRelevantTags() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "filterPersonMeaningTags",
                List.class,
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$PersonFocus")
        );
        method.setAccessible(true);

        final Object personFocus = buildTestPersonFocus("\uC6D0\uBE48");
        final List<String> tags = List.of(
                "[\uC5D4\uC11C\uD074]#\uAE40\uC6D0\uBE48",
                "[\uC5D4\uC11C\uD074]#\uAE40\uAC00\uC601",
                "#\uD68C\uC0AC\uBD88\uB9CC",
                "#dreamdiary"
        );

        @SuppressWarnings("unchecked")
        final List<String> filtered = (List<String>) method.invoke(service, tags, personFocus);

        assertEquals(List.of("[\uC5D4\uC11C\uD074]#\uAE40\uC6D0\uBE48"), filtered);
    }

    /**
     * person-meaning 스니펫은 embedding_text 메타라인과 HTML을 제거해야 합니다.
     */
    @Test
    void sanitizePersonMeaningSnippet_shouldStripMetadataAndHtml() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "sanitizePersonMeaningSnippet",
                RagSearchResult.class,
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$PersonFocus")
        );
        method.setAccessible(true);

        final JournalEntryEmbeddingEntity entity = JournalEntryEmbeddingEntity.builder()
                .journalEntryId(101)
                .embeddingText(
                        "\uC720\uD615: DIARY\n"
                                + "\uB0A0\uC9DC: 2026-01-01\n"
                                + "\uD575\uC2EC \uD0DC\uADF8: [\uC5D4\uC11C\uD074]#\uAE40\uC6D0\uBE48\n"
                                + "\uBCF8\uBB38: <p>\uC624\uB298 \uC6D0\uBE48\uACFC \uD68C\uC758\uD588\uB2E4</p>"
                )
                .build();
        final RagSearchResult result = RagSearchResult.builder().entity(entity).build();
        final Object personFocus = buildTestPersonFocus("\uC6D0\uBE48");

        final String snippet = (String) method.invoke(service, result, personFocus);

        assertFalse(snippet.contains("\uC720\uD615:"));
        assertFalse(snippet.contains("<p>"));
        assertTrue(snippet.contains("\uC6D0\uBE48"));
    }

    /**
     * person-meaning 해석 리드 문단은 태그·역할·기록 유형을 한 문장으로 엮어야 합니다.
     */
    @Test
    void buildPersonMeaningInterpretiveLead_shouldComposeLeadSentence() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
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
                "\uC6D0\uBE48",
                Map.of("[\uC5D4\uC11C\uD074]#\uAE40\uC6D0\uBE48", 3),
                List.of("\uAE34\uC7A5\u00B7\uACBD\uACC4 \uCD95(2)"),
                Map.of("DIARY", 4, "DREAM", 1),
                Map.of("[\uC5D4\uC11C\uD074]#\uC870\uC9C1\uC5ED\uB3D9", 5),
                Map.of("DYNAMICS", 4)
        );

        assertTrue(lead.contains("\uC6D0\uBE48"));
        assertTrue(lead.contains("\uAE40\uC6D0\uBE48"));
        assertTrue(lead.contains("\uAE34\uC7A5"));
        assertTrue(lead.contains("\uC77C\uAE30"));
        assertTrue(lead.contains("\uC870\uC9C1\uC5ED\uB3D9"));
        assertTrue(lead.contains("DYNAMICS"));
    }

    /**
     * person-meaning fallback\uC740 \uC778\uBB3C \uD0DC\uADF8 \uC678 \uC5F0\uACB0 \uB9E5\uB77D \uD0DC\uADF8\uB97C \uC81C\uC678\uD574\uC57C \uD569\uB2C8\uB2E4.
     */
    @Test
    void filterPersonMeaningLinkedContextTags_shouldExcludePersonFocusTags() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "filterPersonMeaningLinkedContextTags",
                List.class,
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$PersonFocus")
        );
        method.setAccessible(true);

        final Object personFocus = buildTestPersonFocus("\uC6D0\uBE48");
        final List<String> tags = List.of(
                "[\uC5D4\uC11C\uD074]#\uAE40\uC6D0\uBE48",
                "[\uC5D4\uC11C\uD074]#\uC870\uC9C1\uC5ED\uB3D9",
                "[\uC5D4\uC11C\uD074]#\uAE40\uC885\uC21C"
        );

        @SuppressWarnings("unchecked")
        final List<String> filtered = (List<String>) method.invoke(service, tags, personFocus);

        assertEquals(
                List.of("[\uC5D4\uC11C\uD074]#\uC870\uC9C1\uC5ED\uB3D9", "[\uC5D4\uC11C\uD074]#\uAE40\uC885\uC21C"),
                filtered
        );
    }

    /**
     * \uC5F0\uACB0 \uB9E5\uB77D \uD0DC\uADF8 \uD575\uC2EC\uC5B4\uB97C \uC778\uC6A9\uD55C person-meaning \uB2F5\uC740 hollow\uAC00 \uC544\uB2C8\uC5B4\uC57C \uD569\uB2C8\uB2E4.
     */
    @Test
    void isHollowPersonMeaningResponse_shouldAcceptLinkedContextTagCitation() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "isHollowPersonMeaningResponse",
                String.class,
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext")
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContextWithTaggedResults("\uC6D0\uBE48");
        final String interpretiveResponse =
                "\uAE30\uB85D\uC0C1 \uC6D0\uBE48\uC740 [\uC5D4\uC11C\uD074]#\uC870\uC9C1\uC5ED\uB3D9 \uD0DC\uADF8\uAC00 \uC790\uC8FC \uAC19\uC774 \uBD99\uB294 "
                        + "DYNAMICS \uC77C\uAE30\uC5D0\uC11C \uC870\uC9C1 \uC5ED\uB3D9 \uB9E5\uB77D\uC758 \uC778\uBB3C\uB85C \uBC18\uBCF5\uB3FC.";

        final boolean hollow = (boolean) method.invoke(service, interpretiveResponse, ragContext);

        assertFalse(hollow);
    }

    /**
     * \uD0DC\uADF8 \uD575\uC2EC\uC5B4 \uC778\uC6A9 \uAC80\uC0AC\uB294 # \uC774\uD6C4 \uBB38\uC790\uC5F4\uB3C4 \uD5C8\uC6A9\uD574\uC57C \uD569\uB2C8\uB2E4.
     */
    @Test
    void citesPersonMeaningTagEvidence_shouldAcceptHashStem() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "citesPersonMeaningTagEvidence",
                String.class,
                Map.class
        );
        method.setAccessible(true);

        final boolean cited = (boolean) method.invoke(
                service,
                "\uC6D0\uBE48\uC740 \uC870\uC9C1\uC5ED\uB3D9 \uB9E5\uB77D\uC5D0\uC11C \uC790\uC8FC \uB4F1\uC7A5\uD569\uB2C8\uB2E4.",
                Map.of("[\uC5D4\uC11C\uD074]#\uC870\uC9C1\uC5ED\uB3D9", 3)
        );

        assertTrue(cited);
    }

    /**
     * person-meaning fallback\uC740 \uC5F0\uACB0 \uB9E5\uB77D \uD0DC\uADF8\uC640 \uCC45\uD130 \uBD84\uB958\uB97C \uD3EC\uD568\uD574\uC57C \uD569\uB2C8\uB2E4.
     */
    @Test
    void buildPersonMeaningDeterministicFallback_shouldIncludeLinkedContextAndChapter() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "buildPersonMeaningDeterministicFallback",
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext")
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContextWithTaggedResults("\uC6D0\uBE48");
        final String fallback = (String) method.invoke(service, ragContext);

        assertTrue(fallback.contains("\uC5F0\uACB0 \uB9E5\uB77D"));
        assertTrue(fallback.contains("\uC870\uC9C1\uC5ED\uB3D9"));
        assertTrue(fallback.contains("\uCC45\uD130 \uBD84\uB958"));
        assertTrue(fallback.contains("DYNAMICS"));
        assertFalse(fallback.toLowerCase().contains("entity catalog"));
    }

    /**
     * TAG 매칭 source는 ENTITY source보다 높은 person focus 우선순위를 가져야 합니다.
     */
    @Test
    void resolvePersonFocusMatchPriority_shouldPreferTagOverEntity() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "resolvePersonFocusMatchPriority",
                RagSearchResult.class,
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$PersonFocus")
        );
        method.setAccessible(true);

        final Object personFocus = buildTestPersonFocus("\uC6D0\uBE48");
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
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "resolvePersonFocusedResults",
                List.class,
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$PersonFocus")
        );
        method.setAccessible(true);

        final Object personFocus = buildTestPersonFocus("\uC6D0\uBE48");
        final JournalEntryEmbeddingEntity entity = JournalEntryEmbeddingEntity.builder()
                .journalEntryId(101)
                .embeddingText("\uBCF8\uBB38: \uC624\uB298 \uC6D0\uBE48\uACFC \uD68C\uC758\uD588\uB2E4")
                .embeddingPayloadJson("{\"tags\":\"[\uC77C\uC0C1]#\uD68C\uC758\"}")
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
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "citesPersonMeaningSnippetEvidence",
                String.class,
                List.class
        );
        method.setAccessible(true);

        final boolean cited = (boolean) method.invoke(
                service,
                "\uAE30\uB85D\uC744 \uBCF4\uBA74 \uC624\uB298 \uC6D0\uBE48\uACFC \uD68C\uC758\uD588\uB2E4.",
                List.of("\uC624\uB298 \uC6D0\uBE48\uACFC \uD68C\uC758\uD588\uB2E4")
        );

        assertTrue(cited);
    }

    private static Object buildTestPersonFocus(final String target) throws Exception {
        final Class<?> personFocusClass = Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$PersonFocus");
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
        final Class<?> personFocusClass = Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$PersonFocus");
        final Class<?> ragContextClass = Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext");

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
        final Class<?> personFocusClass = Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$PersonFocus");
        final Class<?> ragContextClass = Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext");

        final var personFocusCtor = personFocusClass.getDeclaredConstructor(
                String.class,
                List.class,
                int.class,
                JournalEntityFocusService.PersonEntityFocusSummary.class
        );
        personFocusCtor.setAccessible(true);
        final Object personFocus = personFocusCtor.newInstance(target, List.of(target), 1, null);

        final JournalEntryEmbeddingEntity entity = JournalEntryEmbeddingEntity.builder()
                .journalEntryId(101)
                .contentKind("DIARY")
                .embeddingText("\uBCF8\uBB38: \uC6D0\uBE48\uB2D8\uACFC \uD68C\uC758\uD588\uB2E4")
                .embeddingPayloadJson(
                        "{\"tags\":\"[\uC5D4\uC11C\uD074]#\uAE40\uC6D0\uBE48 [\uC5D4\uC11C\uD074]#\uC870\uC9C1\uC5ED\uB3D9\","
                                + "\"chapterCategory\":\"DYNAMICS\"}"
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

    /**
     * "어떻게 생각" 류 질문은 통섭형(SYNTHESIS)으로 분류해야 합니다.
     */
    @Test
    void detectRagIntent_shouldTreatAttitudeQuestionAsSynthesis() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod("detectRagIntent", String.class);
        method.setAccessible(true);

        final RagIntent intent = (RagIntent) method.invoke(
                service,
                "\uB098\uB294 \uC6D0\uBE48\uB2D8\uC744 \uC5B4\uB5BB\uAC8C \uC0DD\uAC01\uD558\uACE0 \uC788\uB2C8?"
        );

        assertEquals(RagIntent.SYNTHESIS, intent);
    }

    /**
     * person-meaning 힌트는 태도/감정 질문도 인식해야 합니다.
     */
    @Test
    void isPersonMeaningQuery_shouldRecognizeHowYouThinkQuestion() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod("isPersonMeaningQuery", String.class);
        method.setAccessible(true);

        final boolean personMeaning = (boolean) method.invoke(
                service,
                "\uB098\uB294 \uC6D0\uBE48\uB2D8\uC744 \uC5B4\uB5BB\uAC8C \uC0DD\uAC01\uD558\uACE0 \uC788\uB2C8?"
        );

        assertTrue(personMeaning);
    }

    /**
     * LOOKUP 인물 질문 프롬프트는 조직 일반론 억제와 인덱스 인용 금지를 포함해야 합니다.
     */
    @Test
    void buildIntentPrompt_shouldConstrainLookupPersonQueries() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "buildIntentPrompt",
                RagIntent.class,
                String.class
        );
        method.setAccessible(true);

        final String prompt = (String) method.invoke(
                service,
                RagIntent.LOOKUP,
                "\uC6D0\uBE48\uB2D8\uC740 \uBB50 \uD588\uC5B4?"
        );

        assertTrue(prompt.contains("\uC870\uC9C1"));
        assertTrue(prompt.contains("[1]"));
    }

    /**
     * 응답 후처리는 RAG 내부 기록 인덱스 인용을 제거해야 합니다.
     */
    @Test
    void stripInternalRecordCitations_shouldRemoveBracketIndexes() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod("stripInternalRecordCitations", String.class);
        method.setAccessible(true);

        final String cleaned = (String) method.invoke(
                service,
                "\uC608\uB97C \uB4E4\uC5B4, [2] \uAE30\uB85D\uC5D0\uC11C\uB294 \uAC1C\uC785\uB3C4\uAC00 \uB192\uC558\uC2B5\uB2C8\uB2E4."
        );

        assertFalse(cleaned.contains("[2]"));
        assertTrue(cleaned.contains("\uAE30\uB85D"));
    }

    /**
     * LOOKUP 인물 태도 질문의 빈 조직 일반론은 degraded로 감지해야 합니다.
     */
    @Test
    void isDegradedPersonResponse_shouldFlagLookupGenericBucketAnswer() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "isDegradedPersonResponse",
                String.class,
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext"),
                String.class
        );
        method.setAccessible(true);

        final Class<?> ragContextClass = Class.forName(
                "io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext"
        );
        final Class<?> personFocusClass = Class.forName(
                "io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$PersonFocus"
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
                "\uC6D0\uBE48\uB2D8\uC740 \uC870\uC9C1 \uB0B4\uC5D0\uC11C \uC911\uC694\uD55C \uC5ED\uD560\uC744 \uD558\uB294 \uC778\uBB3C\uC774\uC5D0\uC694.",
                lookupContext,
                "\uB098\uB294 \uC6D0\uBE48\uB2D8\uC744 \uC5B4\uB5BB\uAC8C \uC0DD\uAC01\uD558\uACE0 \uC788\uB2C8?"
        );

        assertTrue(degraded);
    }

    /**
     * 1인칭 태도 질문은 person-attitude 경로로 분류해야 합니다.
     */
    @Test
    void isPersonAttitudeQuery_shouldRecognizeHowYouThinkQuestion() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod("isPersonAttitudeQuery", String.class);
        method.setAccessible(true);

        final boolean attitude = (boolean) method.invoke(
                service,
                "\uB098\uB294 \uC6D0\uBE48\uB2D8\uC744 \uC5B4\uB5BB\uAC8C \uC0DD\uAC01\uD558\uACE0 \uC788\uB2C8?"
        );

        assertTrue(attitude);
    }

    /**
     * 상징·의미 질문(1인칭 태도 아님)은 person-attitude로 분류하지 않아야 합니다.
     */
    @Test
    void isPersonAttitudeQuery_shouldNotMatchSymbolicMeaningQuestion() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod("isPersonAttitudeQuery", String.class);
        method.setAccessible(true);

        final boolean attitude = (boolean) method.invoke(
                service,
                "\uC6D0\uBE48\uC740 \uB0B4 \uAE30\uB85D\uC5D0\uC11C \uC5B4\uB5A4 \uC758\uBBF8\uC57C?"
        );

        assertFalse(attitude);
    }

    /**
     * SYNTHESIS 태도 질문 프롬프트는 2인칭 비춤·코칭 금지를 포함해야 합니다.
     */
    @Test
    void buildIntentPrompt_shouldUsePersonStanceForAttitudeQuestion() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "buildIntentPrompt",
                RagIntent.class,
                String.class
        );
        method.setAccessible(true);

        final String prompt = (String) method.invoke(
                service,
                RagIntent.SYNTHESIS,
                "\uB098\uB294 \uC6D0\uBE48\uB2D8\uC744 \uC5B4\uB5BB\uAC8C \uC0DD\uAC01\uD558\uACE0 \uC788\uB2C8?"
        );

        assertTrue(prompt.contains("PERSON_STANCE_SCAFFOLD"));
        assertTrue(prompt.contains("\uD0DC\uB3C4"));
        assertTrue(prompt.contains("\uD611\uC5C5"));
    }

    /**
     * 태도 질문에 대한 HR식 성격 평가 답변은 degraded로 감지해야 합니다.
     */
    @Test
    void isDegradedPersonResponse_shouldFlagHrProfileForAttitudeQuestion() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "isDegradedPersonResponse",
                String.class,
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext"),
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = buildSyntheticPersonMeaningRagContext();

        final boolean degraded = (boolean) method.invoke(
                service,
                "\uC6D0\uBE48\uB2D8\uC740 \uB9E4\uC6B0 \uC5F4\uC131\uC801\uC774\uACE0 \uC8FC\uB3D9\uC801\uC778 \uC778\uBB3C\uC785\uB2C8\uB2E4. "
                        + "\uC870\uC9C1 \uC5ED\uB3D9\uC131\uC5D0\uC11C \uC911\uC694\uD55C \uC5ED\uD560\uC744 \uD558\uBA70 \uD611\uC5C5 \uAD00\uACC4\uB97C \uC720\uC9C0\uD558\uB294 \uAC83\uC774 \uC911\uC694\uD560 \uAC83 \uAC19\uC2B5\uB2C8\uB2E4.",
                ragContext,
                "\uB098\uB294 \uC6D0\uBE48\uB2D8\uC744 \uC5B4\uB5BB\uAC8C \uC0DD\uAC01\uD558\uACE0 \uC788\uB2C8?"
        );

        assertTrue(degraded);
    }
}
