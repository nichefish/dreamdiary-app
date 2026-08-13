package io.nicheblog.dreamdiary.feature.ai.rag;

import io.nicheblog.dreamdiary.feature.ai.person.PersonFocus;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.RagSearchResult;

import java.util.List;

/**
 * RAG 의도, 검색 결과, 프롬프트용 컨텍스트 텍스트를 함께 보관한다.
 *
 * <p>채팅 채널·저널 진입점이 동일 구조를 소비한다.</p>
 *
 * @param intent RAG 의도
 * @param results 검색 결과
 * @param text LLM 시스템 프롬프트에 붙일 컨텍스트 텍스트 (없으면 null)
 * @param personFocus 인물 focus (없으면 null)
 */
public record RagContext(
        RagIntent intent,
        List<RagSearchResult> results,
        String text,
        PersonFocus personFocus
) {
    /**
     * 결과·텍스트·focus 없이 의도만 담은 빈 컨텍스트.
     *
     * @param intent RAG 의도
     * @return 빈 컨텍스트
     */
    public static RagContext empty(final RagIntent intent) {
        return new RagContext(intent, List.of(), null, null);
    }
}
