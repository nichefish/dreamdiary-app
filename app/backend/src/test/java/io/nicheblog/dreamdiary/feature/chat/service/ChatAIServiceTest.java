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
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext"),
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContext("\uC6D0\uBE48");
        final String hollowResponse =
                "\uC6D0\uBE48\uC740 \uD300 \uAD00\uACC4\uC640 \uC804\uB7B5\uC801 \uD589\uB3D9 \uCE21\uBA74\uC5D0\uC11C \uC790\uC8FC \uB4F1\uC7A5\uD569\uB2C8\uB2E4.";

        final boolean hollow = (boolean) method.invoke(
                service,
                hollowResponse,
                ragContext,
                "\uC6D0\uBE48\uC740 \uB0B4 \uAE30\uB85D\uC5D0\uC11C \uC5B4\uB5A4 \uC758\uBBF8\uC57C?"
        );

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
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext"),
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContext("\uC6D0\uBE48");
        final String leakedResponse =
                "\uC5ED\uD560 \uCD95 roleaxesko: \uD300 \uB3D9\uB8CC \uBC18\uBCF5 \uCD95 repeated_tags: #dreamdiary";

        final boolean hollow = (boolean) method.invoke(
                service,
                leakedResponse,
                ragContext,
                "\uC6D0\uBE48\uC740 \uB0B4 \uAE30\uB85D\uC5D0\uC11C \uC5B4\uB5A4 \uC758\uBBF8\uC57C?"
        );

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
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext"),
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContext("\uC6D0\uBE48");
        final String noisyResponse = "\uC6D0\uBE48\uC740 #dreamdiary \uD0DC\uADF8\uC640 \uAD00\uB828\uB418\uC5B4 \uB4F1\uC7A5\uD569\uB2C8\uB2E4.";

        final boolean hollow = (boolean) method.invoke(
                service,
                noisyResponse,
                ragContext,
                "\uC6D0\uBE48\uC740 \uB0B4 \uAE30\uB85D\uC5D0\uC11C \uC5B4\uB5A4 \uC758\uBBF8\uC57C?"
        );

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
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext"),
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContextWithTaggedResults("\uC6D0\uBE48");
        final String interpretiveResponse =
                "\uAE30\uB85D\uC0C1 \uC6D0\uBE48\uC740 [\uC5D4\uC11C\uD074]#\uC870\uC9C1\uC5ED\uB3D9 \uD0DC\uADF8\uAC00 \uC790\uC8FC \uAC19\uC774 \uBD99\uB294 "
                        + "DYNAMICS \uC77C\uAE30\uC5D0\uC11C \uC870\uC9C1 \uC5ED\uB3D9 \uB9E5\uB77D\uC758 \uC778\uBB3C\uB85C \uBC18\uBCF5\uB3FC.";

        final boolean hollow = (boolean) method.invoke(
                service,
                interpretiveResponse,
                ragContext,
                "\uC6D0\uBE48\uC740 \uB0B4 \uAE30\uB85D\uC5D0\uC11C \uC5B4\uB5A4 \uC758\uBBF8\uC57C?"
        );

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
     * 내 대화/등장 질문은 person-attitude가 아니어야 합니다.
     */
    @Test
    void isPersonAttitudeQuery_shouldNotMatchDialogueAppearanceQuestion() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method attitudeMethod = ChatAIService.class.getDeclaredMethod("isPersonAttitudeQuery", String.class);
        final Method appearanceMethod = ChatAIService.class.getDeclaredMethod("isPersonAppearanceQuery", String.class);
        attitudeMethod.setAccessible(true);
        appearanceMethod.setAccessible(true);

        final String query = "\uB0B4 \uB300\uD654\uC5D0\uC11C \uAC00\uC601\uB2D8\uC740 \uC5B4\uB5A4 \uB290\uB08C\uC73C\uB85C \uB4F1\uC7A5\uD558\uACE0 \uC788\uB2C8";
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
                "\uB0B4 \uB300\uD654\uC5D0\uC11C \uAC00\uC601\uB2D8\uC740 \uC5B4\uB5A4 \uB290\uB08C\uC73C\uB85C \uB4F1\uC7A5\uD558\uACE0 \uC788\uB2C8"
        );

        assertTrue(prompt.contains("PERSON_MEANING_SCAFFOLD"));
        assertTrue(prompt.contains("\uB4F1\uC7A5"));
        assertFalse(prompt.contains("PERSON_STANCE_SCAFFOLD"));
        assertTrue(prompt.contains("\uCD94\uB860\uD558\uC790\uBA74"));
    }

    /**
     * 인용 나열+성격 단정 person-meaning 답변은 degraded로 감지해야 합니다.
     */
    @Test
    void isDegradedPersonResponse_shouldFlagTraitQuoteParadeForAppearanceQuestion() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "isDegradedPersonResponse",
                String.class,
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext"),
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContextWithTaggedResults("\uAC00\uC601");
        final String shallowResponse =
                "\uAE30\uB85D\uC0C1 #\uAE40\uAC00\uC601 \uCD95\uC5D0 \uBB36\uC5EC \uC788\uACE0, \"\uC77C\uD68C\uC6A9\uC810\uAC00\uB77D.....\" \uB610\uB294 \"\uC218\uBC15\uB9CC \uB370\uB824 \uAC00\uB294\uC911....\" \uB4F1\uC758 \uB9D0\uC744 \uD558\uBA74\uC11C \uB4F1\uC7A5\uD569\uB2C8\uB2E4. "
                        + "\uC774\uB85C \uBD80\uD130 \uCD94\uB860\uD558\uC790\uBA74, \uCE5C\uADFC\uD558\uACE0 \uC790\uC5F0\uC2A4\uB7EC\uC6B4 \uC778\uBB3C\uB85C \uB4F1\uC7A5\uD558\uB294 \uAC83 \uAC19\uC544.";

        final boolean degraded = (boolean) method.invoke(
                service,
                shallowResponse,
                ragContext,
                "\uB0B4 \uB300\uD654\uC5D0\uC11C \uAC00\uC601\uB2D8\uC740 \uC5B4\uB5A4 \uB290\uB08C\uC73C\uB85C \uB4F1\uC7A5\uD558\uACE0 \uC788\uB2C8"
        );

        assertTrue(degraded);
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
        assertTrue(prompt.contains("\uB124\uAC00 \uAE30\uB85D\uC5D0 \uB0A8\uAE34"));
        assertTrue(prompt.contains("\uACE0\uB824\uD560 \uC218 \uC788"));
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

        final Object ragContext = buildTestRagContextWithTaggedResults("\uC6D0\uBE48");

        final boolean degraded = (boolean) method.invoke(
                service,
                "\uC6D0\uBE48\uB2D8\uC740 \uB9E4\uC6B0 \uC5F4\uC131\uC801\uC774\uACE0 \uC8FC\uB3D9\uC801\uC778 \uC778\uBB3C\uC785\uB2C8\uB2E4. "
                        + "\uC870\uC9C1 \uC5ED\uB3D9\uC131\uC5D0\uC11C \uC911\uC694\uD55C \uC5ED\uD560\uC744 \uD558\uBA70 \uD611\uC5C5 \uAD00\uACC4\uB97C \uC720\uC9C0\uD558\uB294 \uAC83\uC774 \uC911\uC694\uD560 \uAC83 \uAC19\uC2B5\uB2C8\uB2E4.",
                ragContext,
                "\uB098\uB294 \uC6D0\uBE48\uB2D8\uC744 \uC5B4\uB5BB\uAC8C \uC0DD\uAC01\uD558\uACE0 \uC788\uB2C8?"
        );

        assertTrue(degraded);
    }

    /**
     * 태그·축이 있는데 사건 나열·심리 라벨만 있는 person-stance 답변은 degraded로 감지해야 합니다.
     */
    @Test
    void isDegradedPersonResponse_shouldFlagEpisodeNarrationWithoutAxisCitation() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "isDegradedPersonResponse",
                String.class,
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext"),
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContextWithTaggedResults("\uC6D0\uBE48");
        final String episodeResponse =
                "\uB124\uAC00 \uAE30\uB85D\uC5D0 \uB0A8\uAE34 \uBC14\uB85C\uB294, \uC6D0\uBE48\uB2D8\uC5D0 \uB300\uD55C \uD0DC\uB3C4\uB294 \uADF8\uB9AC \uAE0D\uC815\uC801\uC774\uC9C0 \uC54A\uC544 \uBCF4\uC778\uB2E4. "
                        + "\uD1F4\uADFC \uC2DC\uAC04\uC774\uC5C8\uB294\uB370 PDF \uD30C\uC2F1 \uC774\uC57C\uAE30\uB97C \uB098\uB204\uB294 \uB3D9\uC548 \uB2E8\uAC00\uAC00 \uC548 \uB098\uC62C \uAC83 \uAC19\uB2E4\uACE0 \uB9D0\uD588\uB2E4. "
                        + "\uADF8\uB7EC\uC790 \uC6D0\uBE48\uB2D8\uC740 \uBC29\uC5B4\uC801\uC73C\uB85C \uB300\uB2F5\uD588\uACE0, \uD2B9\uD788 \uB124 \uB9C8\uC74C\uC5D0\uB294 \uBD88\uC2E0\uACFC \uAC70\uB9AC\uAC10\uC774 \uC788\uB294 \uAC83 \uAC19\uB2E4. "
                        + "\uC5B4\uC918\uB4E0 \uC774\uB294 \uAE30\uB85D\uC5D0 \uB0A8\uAE34 \uB300\uD654\uB9CC\uC73C\uB85C \uCD94\uB860\uD55C \uAC83\uC774\uB2E4.";

        final boolean degraded = (boolean) method.invoke(
                service,
                episodeResponse,
                ragContext,
                "\uB098\uB294 \uC6D0\uBE48\uB2D8\uC744 \uC5B4\uB5BB\uAC8C \uC0DD\uAC01\uD558\uACE0 \uC788\uB2C8?"
        );

        assertTrue(degraded);
    }

    /**
     * person-stance 답변이 태그·축을 인용하면 episode-only 거부 대상이 아니어야 합니다.
     */
    @Test
    void isDegradedPersonResponse_shouldAcceptAxisGroundedStanceAnswer() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "isDegradedPersonResponse",
                String.class,
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext"),
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContextWithTaggedResults("\uC6D0\uBE48");
        final String groundedResponse =
                "\uB124\uAC00 \uAE30\uB85D\uC5D0 \uB0A8\uAE34 \uBC14\uB85C\uB294, \uC6D0\uBE48\uC5D0 \uB300\uD55C \uB9C8\uC74C\uC740 #\uC870\uC9C1\uC5ED\uB3D9 \uCD95\uC5D0\uC11C \uC790\uC8FC \uAE34\uC7A5\uACFC \uC6B0\uB824\uAC00 \uBC18\uBCF5\uB3FC. "
                        + "(1) \uB0B4 \uD0DC\uB3C4\u00B7\uC815\uC11C: \uAE30\uB85D\uC744 \uBCF4\uBA74 \uC6B0\uB824\uAC00 \uB4DC\uB7EC\uB0A8. "
                        + "(2) \uBC18\uBCF5 \uD328\uD134: \uC5C5\uBB34 \uB17C\uC758 \uC7A5\uBA74\uC5D0\uC11C \uB2F5\uC774 \uC5B4\uAE38\uB9AC\uB294 \uBAA8\uC2B5\uC774 \uBC18\uBCF5. "
                        + "(3) \uD568\uAED8 \uBB36\uC778 \uCD95: #\uAE40\uC6D0\uBE48, #\uC870\uC9C1\uC5ED\uB3D9. "
                        + "(4) \uD655\uC815 \uBD88\uAC00: \uC0C1\uB300 \uC131\uACA9\uC740 \uAE30\uB85D\uC5D0 \uC5C6\uC74C.";

        final boolean degraded = (boolean) method.invoke(
                service,
                groundedResponse,
                ragContext,
                "\uB098\uB294 \uC6D0\uBE48\uB2D8\uC744 \uC5B4\uB5BB\uAC8C \uC0DD\uAC01\uD558\uACE0 \uC788\uB2C8?"
        );

        assertFalse(degraded);
    }

    /**
     * 조언·회피·중립화·연결 태그만 인용한 person-stance 답변은 degraded로 감지해야 합니다.
     */
    @Test
    void isDegradedPersonResponse_shouldFlagAdvisoryEvasionAnswer() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "isDegradedPersonResponse",
                String.class,
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext"),
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContextWithTaggedResults("\uC6D0\uBE48");
        final String advisoryResponse =
                "dreamdiary \uAE30\uB85D\uC744 \uD1B5\uD574 \uC54C \uC218 \uC788\uB294 \uBC14\uB85C, \uB2F9\uC2E0\uC740 \uC6D0\uBE48\uB2D8\uACFC \uD568\uAED8 \uC77C\uD558\uBA74\uC11C \uB2E4\uC591\uD55C \uC0C1\uD669\uC5D0\uC11C \uAD50\uB958\uB97C \uAC00\uC9D1\uB2C8\uB2E4. "
                        + "\uADF8\uB7EC\uB098 \uC9C1\uC811\uC801\uC778 \uD3C9\uAC00\uB098 \uC2EC\uB9AC \uC0C1\uD0DC\uB294 \uBA85\uD655\uD788 \uB098\uD0C0\uB098\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4. "
                        + "\uC774\uB7EC\uD55C \uAD00\uACC4\uB97C \uB354 \uAE4A\uAC8C \uC774\uD574\uD558\uAE30 \uC704\uD574\uC11C\uB294 \uBA87 \uAC00\uC9C0 \uC810\uC744 \uACE0\uB824\uD560 \uC218 \uC788\uC2B5\uB2C8\uB2E4: "
                        + "\uC0C1\uD638\uC791\uC6A9 \uD328\uD134: #\uC870\uC9C1\uC5ED\uB3D9 \uCD95\uC5D0 \uBB36\uC5EC \uC788\uC2B5\uB2C8\uB2E4. "
                        + "\uD655\uC815 \uBD88\uAC00: \uB2F9\uC2E0\uC758 \uC0DD\uAC01\uC774\uB098 \uAC10\uC815\uC740 \uBA85\uC2DC\uC801\uC73C\uB85C \uD45C\uD604\uB418\uC9C0 \uC54A\uACE0, \uC911\uB9BD\uC801 \uB610\uB294 \uD3C9\uC628\uD55C \uD0DC\uB3C4\uB97C \uC720\uC9C0\uD558\uACE0 \uC788\uB294 \uAC83\uC73C\uB85C \uBCF4\uC785\uB2C8\uB2E4.";

        final boolean degraded = (boolean) method.invoke(
                service,
                advisoryResponse,
                ragContext,
                "\uB098\uB294 \uC6D0\uBE48\uB2D8\uC744 \uC5B4\uB5BB\uAC8C \uC0DD\uAC01\uD558\uACE0 \uC788\uB2C8?"
        );

        assertTrue(degraded);
    }

    /**
     * SYNTHESIS + personFocus + person-meaning 질문은 RULE_PRIMARY 경로를 써야 합니다.
     */
    @Test
    void shouldUseRulePrimaryPersonSynthesisResponse_shouldBeTrueForAttitudeQuestion() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "shouldUseRulePrimaryPersonSynthesisResponse",
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext"),
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContext("\uC6D0\uBE48");
        final boolean rulePrimary = (boolean) method.invoke(
                service,
                ragContext,
                "\uB098\uB294 \uC6D0\uBE48\uB2D8\uC744 \uC5B4\uB5BB\uAC8C \uC0DD\uAC01\uD558\uACE0 \uC788\uB2C8?"
        );

        assertTrue(rulePrimary);
    }

    /**
     * personFocus 없는 SYNTHESIS 질문은 RULE_PRIMARY를 쓰지 않아야 합니다.
     */
    @Test
    void shouldUseRulePrimaryPersonSynthesisResponse_shouldBeFalseWithoutPersonFocus() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "shouldUseRulePrimaryPersonSynthesisResponse",
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext"),
                String.class
        );
        method.setAccessible(true);

        final Class<?> ragContextClass = Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext");
        final var ragContextCtor = ragContextClass.getDeclaredConstructor(
                RagIntent.class,
                List.class,
                String.class,
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$PersonFocus")
        );
        ragContextCtor.setAccessible(true);
        final Object ragContext = ragContextCtor.newInstance(RagIntent.SYNTHESIS, List.of(), "ctx", null);

        final boolean rulePrimary = (boolean) method.invoke(
                service,
                ragContext,
                "\uC6D0\uBE48\uC740 \uB0B4 \uAE30\uB85D\uC5D0\uC11C \uC5B4\uB5A4 \uC758\uBBF8\uC57C?"
        );

        assertFalse(rulePrimary);
    }

    /**
     * 사용자 표시용 태그 문자열은 [엔서클] 접두를 제거해야 합니다.
     */
    @Test
    void formatDisplayTag_shouldStripMetaPrefix() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod("formatDisplayTag", String.class);
        method.setAccessible(true);

        final String display = (String) method.invoke(service, "[\uC5D4\uC11C\uD074]#\uC870\uC9C1\uC5ED\uB3D9");

        assertEquals("#\uC870\uC9C1\uC5ED\uB3D9", display);
    }

    /**
     * appearance fallback은 4단 구조와 표시용 태그를 포함해야 합니다.
     */
    @Test
    void buildPersonAppearanceDeterministicFallback_shouldUseFourSectionShape() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "buildPersonAppearanceDeterministicFallback",
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext")
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContextWithTaggedResults("\uAC00\uC601");
        final String response = (String) method.invoke(service, ragContext);

        assertTrue(response.contains("(1) \uB4F1\uC7A5 \uB290\uB08C"));
        assertTrue(response.contains("(2) \uBC18\uBCF5 \uB9E5\uB77D"));
        assertTrue(response.contains("(3) \uD568\uAED8 \uBB36\uC778 \uCD95"));
        assertTrue(response.contains("(4) \uD655\uC815 \uBD88\uAC00"));
        assertFalse(response.contains("[\uC5D4\uC11C\uD074]"));
    }

    /**
     * appearance 질문 토큰 추출 시 범위어(대화)보다 인물명(가영)을 남겨야 합니다.
     */
    @Test
    void extractPersonFocusTokens_shouldPreferPersonNameOverDialogueScopeWord() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method extractMethod = ChatAIService.class.getDeclaredMethod("extractPersonFocusTokens", String.class);
        extractMethod.setAccessible(true);

        @SuppressWarnings("unchecked")
        final List<String> tokens = (List<String>) extractMethod.invoke(
                service,
                "\uB0B4 \uB300\uD654\uC5D0\uC11C \uAC00\uC601\uB2D8\uC740 \uC5B4\uB5A4 \uB290\uB08C\uC73C\uB85C \uB4F1\uC7A5\uD558\uACE0 \uC788\uB2C8"
        );

        assertTrue(tokens.contains("\uAC00\uC601"));
        assertFalse(tokens.contains("\uB300\uD654"));
    }

    /**
     * primary person token은 질문의 님 호칭 토큰을 우선해야 합니다.
     */
    @Test
    void selectPrimaryPersonTokenFromQuery_shouldPreferHonorificPersonToken() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "selectPrimaryPersonTokenFromQuery",
                String.class,
                List.class
        );
        method.setAccessible(true);

        final String primary = (String) method.invoke(
                service,
                "\uB0B4 \uB300\uD654\uC5D0\uC11C \uAC00\uC601\uB2D8\uC740 \uC5B4\uB5A4 \uB290\uB08C\uC73C\uB85C \uB4F1\uC7A5\uD558\uACE0 \uC788\uB2C8",
                List.of("\uAC00\uC601", "Dreamdiary")
        );

        assertEquals("\uAC00\uC601", primary);
    }

    /**
     * dominant stem이 있으면 짧은 토큰 오매칭 태그(#문가영)를 제외해야 합니다.
     */
    @Test
    void isPersonRelevantTag_shouldUseDominantStemForShortPersonToken() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "isPersonRelevantTag",
                String.class,
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$PersonFocus"),
                String.class
        );
        method.setAccessible(true);

        final Object personFocus = buildTestPersonFocus("\uAC00\uC601");
        final boolean matchesKayoung = (boolean) method.invoke(
                service,
                "[\uC5D4\uC11C\uD074]#\uAE40\uAC00\uC601",
                personFocus,
                "\uAE40\uAC00\uC601"
        );
        final boolean matchesMoongayoung = (boolean) method.invoke(
                service,
                "[\uC720\uBA85\uC778]#\uBB38\uAC00\uC601",
                personFocus,
                "\uAE40\uAC00\uC601"
        );

        assertTrue(matchesKayoung);
        assertFalse(matchesMoongayoung);
    }

    /**
     * interpretive lead는 표시용 태그 포맷([엔서클] 제거)을 써야 합니다.
     */
    @Test
    void buildPersonMeaningInterpretiveLead_shouldUseDisplayTagFormat() throws Exception {
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
                "\uAC00\uC601",
                Map.of("[\uC5D4\uC11C\uD074]#\uAE40\uAC00\uC601", 24),
                List.of(),
                Map.of(),
                Map.of("[\uC5D4\uC11C\uD074]#\uC870\uC9C1\uC5ED\uB3D9", 9),
                Map.of()
        );

        assertTrue(lead.contains("#\uAE40\uAC00\uC601"));
        assertFalse(lead.contains("[\uC5D4\uC11C\uD074]"));
    }

    /**
     * person focus 표시 이름은 entity catalog canonical label을 우선해야 합니다.
     */
    @Test
    void resolvePersonFocusTarget_shouldPreferEntitySummaryCanonicalLabel() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "resolvePersonFocusTarget",
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$PersonFocus")
        );
        method.setAccessible(true);

        final JournalEntityFocusService.PersonEntityFocusSummary entitySummary =
                new JournalEntityFocusService.PersonEntityFocusSummary(
                        12,
                        "\uAE40\uAC00\uC601",
                        "\uAE40\uAC00\uC601",
                        List.of("\uAC00\uC601"),
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
                "io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$PersonFocus"
        );
        final var personFocusCtor = personFocusClass.getDeclaredConstructor(
                String.class,
                List.class,
                int.class,
                JournalEntityFocusService.PersonEntityFocusSummary.class
        );
        personFocusCtor.setAccessible(true);
        final Object personFocus = personFocusCtor.newInstance(
                "\uAC00\uC601",
                List.of("\uAC00\uC601"),
                5,
                entitySummary
        );

        final String target = (String) method.invoke(service, personFocus);

        assertEquals("\uAE40\uAC00\uC601", target);
    }

    /**
     * tag-only person-meaning 결과는 TAG match type만 포함해야 합니다.
     */
    @Test
    void isTagOnlyPersonMeaningResults_shouldBeTrueForTagMatchesOnly() throws Exception {
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
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
        final ChatAIService service = new ChatAIService(null, null, null, null, null, null, null);
        final Method method = ChatAIService.class.getDeclaredMethod(
                "buildPersonSynthesisHybridSystemPrompt",
                Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext"),
                String.class
        );
        method.setAccessible(true);

        final Object ragContext = buildTestRagContextWithTaggedResults("\uC6D0\uBE48");
        final String prompt = (String) method.invoke(
                service,
                ragContext,
                "\uB098\uB294 \uC6D0\uBE48\uB2D8\uC744 \uC5B4\uB5BB\uAC8C \uC0DD\uAC01\uD558\uACE0 \uC788\uB2C8?"
        );

        assertTrue(prompt.contains("PERSON_SYNTHESIS_HYBRID"));
        assertTrue(prompt.contains("SNAPSHOT"));
        assertTrue(prompt.contains("(1) \uB0B4 \uD0DC\uB3C4"));
        assertFalse(prompt.contains("[\uC5D4\uC11C\uD074]"));
    }
}
