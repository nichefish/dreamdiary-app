package io.nicheblog.dreamdiary.feature.ai.guard;

import io.nicheblog.dreamdiary.feature.ai.AiRagTestSupport;
import io.nicheblog.dreamdiary.feature.ai.person.PersonFocusResolver;
import io.nicheblog.dreamdiary.feature.ai.person.PersonSnapshotService;
import io.nicheblog.dreamdiary.feature.ai.rag.RagContext;
import io.nicheblog.dreamdiary.feature.ai.rag.RagSearchFacade;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ResponseGuardService person-meaning hollow·태도 rich-trust 게이트 계약 테스트.
 */
class ResponseGuardServiceTest {

    /**
     * 테스트용 ResponseGuardService. person 스냅샷 집계기를 실 협력자로 주입한다.
     */
    private static ResponseGuardService newGuardService() {
        final RagSearchFacade ragSearchFacade = new RagSearchFacade(null, null);
        final PersonFocusResolver personFocusResolver = new PersonFocusResolver(null, null, ragSearchFacade);
        final PersonSnapshotService personSnapshotService = new PersonSnapshotService(personFocusResolver);
        return new ResponseGuardService(personSnapshotService);
    }

    /**
     * 태그·역할 축 없이 빈 주제 분류만 있는 person-meaning 답변은 hollow로 판정해야 합니다.
     */
    @Test
    void isHollowPersonMeaningResponse_shouldDetectGenericBuckets() throws Exception {
        final ResponseGuardService service = newGuardService();

        final RagContext ragContext = (RagContext) AiRagTestSupport.buildTestRagContext("민수");
        final String hollowResponse =
                "민수는 팀 관계와 전략적 행동 측면에서 자주 등장합니다.";

        final boolean hollow = service.isHollowPersonMeaningResponse(
                hollowResponse,
                ragContext.personFocus(),
                ragContext.results(),
                ragContext.intent(),
                "민수는 내 기록에서 어떤 의미야?"
        );

        assertTrue(hollow);
    }
    /**
     * 스캐폴드 메타 필드 유출 응답은 degraded로 판정해야 합니다.
     */
    @Test
    void isHollowPersonMeaningResponse_shouldRejectScaffoldLeak() throws Exception {
        final ResponseGuardService service = newGuardService();

        final RagContext ragContext = (RagContext) AiRagTestSupport.buildTestRagContext("민수");
        final String leakedResponse =
                "역할 축 roleaxesko: 팀 동료 반복 축 repeated_tags: #dreamdiary";

        final boolean hollow = service.isHollowPersonMeaningResponse(
                leakedResponse,
                ragContext.personFocus(),
                ragContext.results(),
                ragContext.intent(),
                "민수는 내 기록에서 어떤 의미야?"
        );

        assertTrue(hollow);
    }
    /**
     * 스냅샷에 없는 dreamdiary 잡음 태그만 인용한 응답도 degraded로 봐야 합니다.
     */
    @Test
    void isHollowPersonMeaningResponse_shouldRejectDreamdiaryNoiseTag() throws Exception {
        final ResponseGuardService service = newGuardService();

        final RagContext ragContext = (RagContext) AiRagTestSupport.buildTestRagContext("민수");
        final String noisyResponse = "민수는 #dreamdiary 태그와 관련되어 등장합니다.";

        final boolean hollow = service.isHollowPersonMeaningResponse(
                noisyResponse,
                ragContext.personFocus(),
                ragContext.results(),
                ragContext.intent(),
                "민수는 내 기록에서 어떤 의미야?"
        );

        assertTrue(hollow);
    }
    /**
     * 연결 맥락 태그 핵심어를 인용한 person-meaning 답은 hollow가 아니어야 합니다.
     */
    @Test
    void isHollowPersonMeaningResponse_shouldAcceptLinkedContextTagCitation() throws Exception {
        final ResponseGuardService service = newGuardService();

        final RagContext ragContext = (RagContext) AiRagTestSupport.buildTestRagContextWithTaggedResults("민수");
        final String interpretiveResponse =
                "기록상 민수는 [엔서클]#조직역동 태그가 자주 같이 붙는 "
                        + "회고 말머리의 일기에서 조직 역동 맥락의 인물로 반복돼.";

        final boolean hollow = service.isHollowPersonMeaningResponse(
                interpretiveResponse,
                ragContext.personFocus(),
                ragContext.results(),
                ragContext.intent(),
                "민수는 내 기록에서 어떤 의미야?"
        );

        assertFalse(hollow);
    }
    /**
     * 태그 핵심어 인용 검사는 # 이후 문자열도 허용해야 합니다.
     */
    @Test
    void citesPersonMeaningTagEvidence_shouldAcceptHashStem() throws Exception {
        final ResponseGuardService service = newGuardService();

        final boolean cited = service.citesPersonMeaningTagEvidence(
                "민수는 조직역동 맥락에서 자주 등장합니다.",
                Map.of("[엔서클]#조직역동", 3)
        );

        assertTrue(cited);
    }
    /**
     * person 태그가 없을 때 근거 스니펫 인용은 guard 증거로 인정해야 합니다.
     */
    @Test
    void citesPersonMeaningSnippetEvidence_shouldDetectSnippetOverlap() throws Exception {
        final ResponseGuardService service = newGuardService();

        final boolean cited = service.citesPersonMeaningSnippetEvidence(
                "기록을 보면 오늘 민수와 회의했다.",
                List.of("오늘 민수와 회의했다")
        );

        assertTrue(cited);
    }
    /**
     * 풍부 신뢰 게이트: 기록 근거 없는 빈 조직 버킷 나열 태도 답변은 거부해야 합니다.
     */
    @Test
    void isDegradedPersonStanceRichResponse_shouldRejectGenericBucket() throws Exception {
        final ResponseGuardService service = newGuardService();

        final boolean degraded = service.isDegradedPersonStanceRichResponse(
                "민수님은 조직 내에서 중요한 역할을 하며 업무 협업에 기여하는 것으로 보입니다."
        );

        assertTrue(degraded);
    }
    /**
     * 풍부 신뢰 게이트: 기록 근거가 담긴 긴 산문 태도 답변은 통과해야 합니다.
     */
    @Test
    void isDegradedPersonStanceRichResponse_shouldAcceptGroundedProse() throws Exception {
        final ResponseGuardService service = newGuardService();

        final boolean degraded = service.isDegradedPersonStanceRichResponse(
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
        final ResponseGuardService service = newGuardService();

        final String code = service.describePersonStanceRichGuardFailure(
                "민수님은 조직 내에서 중요한 역할을 하며 업무 협업에 기여하는 것으로 보이며 전략적 존재감이 있습니다."
        );

        assertEquals("person_stance_generic_bucket", code);
    }
}
