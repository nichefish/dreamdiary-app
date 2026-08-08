package io.nicheblog.dreamdiary.feature.ai.rag;

/**
 * RAG 검색 폭·임계값 한도.
 *
 * <p>채널 {@code chat_setting} 관리자 값을 오케스트레이터가 매핑해 넘긴다.
 * {@code feature.ai}는 chat 설정 서비스에 의존하지 않는다.</p>
 *
 * @param enabled RAG 사용 여부
 * @param topK LOOKUP 최대 건수
 * @param minScore LOOKUP/SUMMARY 벡터 최소 점수
 * @param summaryTopK SUMMARY 최대 건수
 * @param synthesisTopK SYNTHESIS(비태도) 최대 건수
 * @param stanceTopK person-stance(태도) 최대 건수
 * @param synthesisMinScore SYNTHESIS 벡터 최소 점수
 */
public record RagSearchLimits(
        boolean enabled,
        int topK,
        double minScore,
        int summaryTopK,
        int synthesisTopK,
        int stanceTopK,
        double synthesisMinScore
) {
}
