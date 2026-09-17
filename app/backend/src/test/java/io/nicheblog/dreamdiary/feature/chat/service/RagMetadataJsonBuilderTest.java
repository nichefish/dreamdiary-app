package io.nicheblog.dreamdiary.feature.chat.service;

import io.nicheblog.dreamdiary.feature.ai.AiRagTestSupport;
import io.nicheblog.dreamdiary.feature.ai.person.PersonFocusResolver;
import io.nicheblog.dreamdiary.feature.ai.person.PersonSnapshotService;
import io.nicheblog.dreamdiary.feature.ai.rag.RagContext;
import io.nicheblog.dreamdiary.feature.ai.rag.RagContextService;
import io.nicheblog.dreamdiary.feature.ai.rag.RagContextTextBuilder;
import io.nicheblog.dreamdiary.feature.ai.rag.RagSearchFacade;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RagMetadataJsonBuilder RAG 출처 메타데이터 JSON 조립 계약 테스트.
 */
class RagMetadataJsonBuilderTest {

    /**
     * 테스트용 RagMetadataJsonBuilder. 실 협력자(RagContextService·PersonSnapshotService·PersonFocusResolver)를 주입한다.
     */
    private static RagMetadataJsonBuilder newMetadataBuilder() {
        final RagSearchFacade ragSearchFacade = new RagSearchFacade(null, null);
        final PersonFocusResolver personFocusResolver = new PersonFocusResolver(null, null, ragSearchFacade);
        final PersonSnapshotService personSnapshotService = new PersonSnapshotService(personFocusResolver);
        final RagContextTextBuilder ragContextTextBuilder = new RagContextTextBuilder(personFocusResolver, personSnapshotService);
        final RagContextService ragContextService = new RagContextService(
                ragSearchFacade, null, null, personFocusResolver, ragContextTextBuilder);
        return new RagMetadataJsonBuilder(ragContextService, personSnapshotService, personFocusResolver);
    }

    /**
     * hybrid 폴백 시 재시도 가드 사유는 metadataJson.retryGuardDetail에 저장해야 합니다.
     */
    @Test
    void buildRagMetadataJson_shouldIncludeRetryGuardDetail() throws Exception {
        final RagMetadataJsonBuilder builder = newMetadataBuilder();

        final RagContext ragContext = (RagContext) AiRagTestSupport.buildTestRagContextWithTaggedResults("민수");
        final String json = builder.buildRagMetadataJson(
                ragContext,
                "RULE_PRIMARY",
                "person_stance_generic_bucket",
                "person_stance_too_short"
        );

        assertNotNull(json);
        assertTrue(json.contains("\"guardDetail\":\"person_stance_generic_bucket\""));
        assertTrue(json.contains("\"retryGuardDetail\":\"person_stance_too_short\""));
    }
}
