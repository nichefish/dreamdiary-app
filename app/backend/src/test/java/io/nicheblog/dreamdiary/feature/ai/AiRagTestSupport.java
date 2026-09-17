package io.nicheblog.dreamdiary.feature.ai;

import io.nicheblog.dreamdiary.feature.ai.rag.RagContext;
import io.nicheblog.dreamdiary.feature.ai.rag.RagIntent;
import io.nicheblog.dreamdiary.feature.journal.embedding.entity.JournalEntryEmbeddingEntity;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.RagSearchResult;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.service.JournalEntityFocusService;

import java.util.ArrayList;
import java.util.List;

/**
 * AI RAG 테스트 공통 지원 유틸.
 *
 * <pre>
 *  package-private 생성자를 가진 PersonFocus·RagContext를 리플렉션으로 조립해 테스트 픽스처를 만든다.
 *  person 관련 서비스 테스트(RAG 컨텍스트·태그 결과)가 재사용한다.
 * </pre>
 *
 * @author nichefish
 */
public final class AiRagTestSupport {

    private AiRagTestSupport() {
    }

    public static String fixturePersonTagFor(final String target) {
        if (AiPersonTestFixtures.PERSON_B.equals(target)) {
            return AiPersonTestFixtures.PERSON_B_TAG;
        }
        return AiPersonTestFixtures.PERSON_A_TAG;
    }

    public static Object buildTestPersonFocus(final String target) throws Exception {
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

    public static Object buildTestRagContext(final String target) throws Exception {
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

    public static Object buildTestRagContextWithTaggedResults(final String target) throws Exception {
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

    public static Object buildTestRagContextWithManyTaggedResults(final String target, final int count) throws Exception {
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
}
