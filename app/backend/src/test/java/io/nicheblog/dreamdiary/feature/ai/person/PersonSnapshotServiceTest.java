package io.nicheblog.dreamdiary.feature.ai.person;

import io.nicheblog.dreamdiary.feature.ai.AiRagTestSupport;
import io.nicheblog.dreamdiary.feature.ai.rag.RagContext;
import io.nicheblog.dreamdiary.feature.ai.rag.RagSearchFacade;
import io.nicheblog.dreamdiary.feature.journal.embedding.entity.JournalEntryEmbeddingEntity;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.RagSearchResult;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.type.JournalEntityRoleType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PersonSnapshotService 역할 축·스니펫 정리·태그 집계·SNAPSHOT 계약 테스트.
 */
class PersonSnapshotServiceTest {

    /**
     * 테스트용 PersonSnapshotService. 실 PersonFocusResolver를 주입한다.
     */
    private static PersonSnapshotService newSnapshotService() {
        final RagSearchFacade ragSearchFacade = new RagSearchFacade(null, null);
        final PersonFocusResolver personFocusResolver = new PersonFocusResolver(null, null, ragSearchFacade);
        return new PersonSnapshotService(personFocusResolver);
    }

    /**
     * entity catalog 역할 축은 한국어 해석 라벨로 변환되어야 합니다.
     */
    @Test
    void formatPersonRoleAxis_shouldUseKoreanAxisLabel() throws Exception {
        final PersonSnapshotService service = newSnapshotService();

        final String label = service.formatPersonRoleAxis(JournalEntityRoleType.TENSION, 3);

        assertTrue(label.contains("긴장"));
        assertTrue(label.contains("(3)"));
    }
    /**
     * person-meaning 스니펫은 embedding_text 메타라인과 HTML을 제거해야 합니다.
     */
    @Test
    void sanitizePersonMeaningSnippet_shouldStripMetadataAndHtml() throws Exception {
        final PersonSnapshotService service = newSnapshotService();

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
        final PersonFocus personFocus = (PersonFocus) AiRagTestSupport.buildTestPersonFocus("민수");

        final String snippet = service.sanitizePersonMeaningSnippet(result, personFocus);

        assertFalse(snippet.contains("유형:"));
        assertFalse(snippet.contains("<p>"));
        assertTrue(snippet.contains("민수"));
    }
    /**
     * person-meaning 집계는 person 태그가 없으면 본문 언급 source로 대체하지 않아야 합니다.
     */
    @Test
    void resolvePersonFocusedResults_shouldNotFallbackToBodyMentionWithoutPersonTag() throws Exception {
        final PersonSnapshotService service = newSnapshotService();

        final PersonFocus personFocus = (PersonFocus) AiRagTestSupport.buildTestPersonFocus("민수");
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
        final List<RagSearchResult> focused = service.resolvePersonFocusedResults(
                List.of(bodyMentionOnly),
                personFocus
        );

        assertTrue(focused.isEmpty());
    }
    /**
     * tag-only person-meaning 결과는 TAG match type만 포함해야 합니다.
     */
    @Test
    void isTagOnlyPersonMeaningResults_shouldBeTrueForTagMatchesOnly() throws Exception {
        final PersonSnapshotService service = newSnapshotService();

        final RagSearchResult tagResult = RagSearchResult.builder()
                .matchType(RagSearchResult.MATCH_TYPE_TAG)
                .score(1.0D)
                .build();
        final RagSearchResult keywordResult = RagSearchResult.builder()
                .matchType(RagSearchResult.MATCH_TYPE_KEYWORD)
                .score(1.0D)
                .build();

        final boolean tagOnly = service.isTagOnlyPersonMeaningResults(List.of(tagResult));
        final boolean mixed = service.isTagOnlyPersonMeaningResults(List.of(tagResult, keywordResult));

        assertTrue(tagOnly);
        assertFalse(mixed);
    }
    /**
     * 태도 질문 SNAPSHOT은 근거 장면을 더 많이·고르게 샘플링해야 합니다.
     */
    @Test
    void buildPersonMeaningSnapshot_stanceQuery_shouldSpreadRichEvidence() throws Exception {
        final PersonSnapshotService service = newSnapshotService();

        final RagContext ragContext = (RagContext) AiRagTestSupport.buildTestRagContextWithManyTaggedResults("민수", 12);
        final PersonMeaningSnapshot snapshot = service.build(
                ragContext.results(),
                ragContext.personFocus(),
                "나는 민수님을 어떻게 생각하고 있니?"
        );

        final List<String> evidenceSnippets = snapshot.evidenceSnippets();

        assertEquals(12, evidenceSnippets.size());
        assertTrue(evidenceSnippets.stream().anyMatch(snippet -> snippet.contains("장면0")));
        assertTrue(evidenceSnippets.stream().anyMatch(snippet -> snippet.contains("장면11")));
    }
}
