package io.nicheblog.dreamdiary.feature.ai.person;

import io.nicheblog.dreamdiary.feature.ai.AiRagTestSupport;
import io.nicheblog.dreamdiary.feature.ai.rag.RagSearchFacade;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.RagSearchResult;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.service.JournalEntityFocusService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PersonFocusResolver 토큰 병합·태그 필터·표시 태그·매칭 우선순위 계약 테스트.
 */
class PersonFocusResolverTest {

    /**
     * 테스트용 PersonFocusResolver. RAG 병합은 RagSearchFacade에 위임한다.
     */
    private static PersonFocusResolver newFocusResolver() {
        return new PersonFocusResolver(null, null, new RagSearchFacade(null, null));
    }

    /**
     * Entity-backed person focus should keep the canonical label and repeated surface forms
     * in the merged token list so tag-only person-meaning retrieval can match alias tags.
     */
    @Test
    void mergePersonFocusTokens_shouldIncludeCanonicalLabelAndSurfaceForms() throws Exception {
        final PersonFocusResolver service = newFocusResolver();

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
        final List<String> mergedTokens = service.mergePersonFocusTokens(
                List.of("민수"),
                entitySummary
        );

        assertEquals(List.of("민수", "민수님"), mergedTokens);
    }
    /**
     * person-meaning 반복 축에는 인물 토큰이 포함된 태그만 남겨야 합니다.
     */
    @Test
    void filterPersonMeaningTags_shouldKeepOnlyPersonRelevantTags() throws Exception {
        final PersonFocusResolver service = newFocusResolver();

        final PersonFocus personFocus = (PersonFocus) AiRagTestSupport.buildTestPersonFocus("민수");
        final List<String> tags = List.of(
                "[엔서클]#김민수",
                "[엔서클]#박지연",
                "#회사불만",
                "#dreamdiary"
        );

        @SuppressWarnings("unchecked")
        final List<String> filtered = service.filterPersonMeaningTags(tags, personFocus);

        assertEquals(List.of("[엔서클]#김민수"), filtered);
    }
    /**
     * person-meaning fallback은 인물 태그 외 연결 맥락 태그를 제외해야 합니다.
     */
    @Test
    void filterPersonMeaningLinkedContextTags_shouldExcludePersonFocusTags() throws Exception {
        final PersonFocusResolver service = newFocusResolver();

        final PersonFocus personFocus = (PersonFocus) AiRagTestSupport.buildTestPersonFocus("민수");
        final List<String> tags = List.of(
                "[엔서클]#김민수",
                "[엔서클]#조직역동",
                "[엔서클]#김종순"
        );

        @SuppressWarnings("unchecked")
        final List<String> filtered = service.filterPersonMeaningLinkedContextTags(tags, personFocus);

        assertEquals(
                List.of("[엔서클]#조직역동", "[엔서클]#김종순"),
                filtered
        );
    }
    /**
     * TAG 매칭 source는 ENTITY source보다 높은 person focus 우선순위를 가져야 합니다.
     */
    @Test
    void resolvePersonFocusMatchPriority_shouldPreferTagOverEntity() throws Exception {
        final PersonFocusResolver service = newFocusResolver();

        final PersonFocus personFocus = (PersonFocus) AiRagTestSupport.buildTestPersonFocus("민수");
        final RagSearchResult tagResult = RagSearchResult.builder()
                .matchType(RagSearchResult.MATCH_TYPE_TAG)
                .build();
        final RagSearchResult entityResult = RagSearchResult.builder()
                .matchType(RagSearchResult.MATCH_TYPE_ENTITY)
                .build();

        final int tagPriority = service.resolvePersonFocusMatchPriority(tagResult, personFocus);
        final int entityPriority = service.resolvePersonFocusMatchPriority(entityResult, personFocus);

        assertTrue(tagPriority > entityPriority);
    }
    /**
     * 사용자 표시용 태그 문자열은 [엔서클] 접두를 제거해야 합니다.
     */
    @Test
    void formatDisplayTag_shouldStripMetaPrefix() throws Exception {
        final PersonFocusResolver service = newFocusResolver();

        final String display = service.formatDisplayTag("[엔서클]#조직역동");

        assertEquals("#조직역동", display);
    }
    /**
     * primary person token은 질문의 님 호칭 토큰을 우선해야 합니다.
     */
    @Test
    void selectPrimaryPersonTokenFromQuery_shouldPreferHonorificPersonToken() throws Exception {
        final PersonFocusResolver service = newFocusResolver();

        final String primary = service.selectPrimaryPersonTokenFromQuery(
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
        final PersonFocusResolver service = newFocusResolver();

        final PersonFocus personFocus = (PersonFocus) AiRagTestSupport.buildTestPersonFocus("지연");
        final boolean matchesPrimaryTag = service.isPersonRelevantTag(
                "[엔서클]#박지연",
                personFocus,
                "박지연"
        );
        final boolean matchesFalsePositiveTag = service.isPersonRelevantTag(
                "[유명인]#문지연",
                personFocus,
                "박지연"
        );

        assertTrue(matchesPrimaryTag);
        assertFalse(matchesFalsePositiveTag);
    }

    /**
     * person focus 표시 이름은 entity catalog canonical label을 우선해야 합니다.
     */
    @Test
    void resolvePersonFocusTarget_shouldPreferEntitySummaryCanonicalLabel() throws Exception {
        final PersonFocusResolver service = newFocusResolver();

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

        final String target = service.resolvePersonFocusTarget((PersonFocus) personFocus);

        assertEquals("박지연", target);
    }
}
