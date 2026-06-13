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
        final Method method = ChatAIService.class.getDeclaredMethod("buildIntentPrompt", RagIntent.class);
        method.setAccessible(true);

        final String prompt = (String) method.invoke(service, RagIntent.SYNTHESIS);

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
                Map.class
        );
        method.setAccessible(true);

        final String lead = (String) method.invoke(
                service,
                "\uC6D0\uBE48",
                Map.of("[\uC5D4\uC11C\uD074]#\uAE40\uC6D0\uBE48", 3),
                List.of("\uAE34\uC7A5\u00B7\uACBD\uACC4 \uCD95(2)"),
                Map.of("DIARY", 4, "DREAM", 1)
        );

        assertTrue(lead.contains("\uC6D0\uBE48"));
        assertTrue(lead.contains("\uAE40\uC6D0\uBE48"));
        assertTrue(lead.contains("\uAE34\uC7A5"));
        assertTrue(lead.contains("\uC77C\uAE30"));
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
                "\uAE30\uB85D\uC744 \uBCF4\uBA74 \uC624\uB298 \uC6D0\uBE48\uACFC \uD68C\uC758\uD558\uB294 \uC7A5\uBA74\uC774 \uC788\uC5B4.",
                List.of("\uC624\uB298 \uC6D0\uBE48\uACFC \uD68C\uC758\uD588\uB2E4")
        );

        assertTrue(cited);
    }

    private static Object buildTestPersonFocus(final String target) throws Exception {
        final Class<?> personFocusClass = Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$PersonFocus");
        return personFocusClass.getConstructor(
                String.class,
                List.class,
                int.class,
                JournalEntityFocusService.PersonEntityFocusSummary.class
        ).newInstance(target, List.of(target), 1, null);
    }

    private static Object buildTestRagContext(final String target) throws Exception {
        final Class<?> personFocusClass = Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$PersonFocus");
        final Class<?> ragContextClass = Class.forName("io.nicheblog.dreamdiary.feature.chat.service.ChatAIService$RagContext");

        final Object personFocus = personFocusClass.getConstructor(
                String.class,
                List.class,
                int.class,
                JournalEntityFocusService.PersonEntityFocusSummary.class
        ).newInstance(target, List.of(target), 1, null);

        return ragContextClass.getConstructor(
                RagIntent.class,
                List.class,
                String.class,
                personFocusClass
        ).newInstance(RagIntent.SYNTHESIS, List.of(), "ctx", personFocus);
    }
}
