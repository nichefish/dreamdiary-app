package io.nicheblog.dreamdiary.feature.journal.embedding.model;

import io.nicheblog.dreamdiary.feature.journal.embedding.entity.JournalEntryEmbeddingEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * RAG 검색 결과.
 *
 * <p>검색된 엔트리뿐 아니라 왜 선택됐는지 설명할 수 있는 검색 메타데이터를 함께 담는다.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagSearchResult {

    /** 직접 키워드 매칭 */
    public static final String MATCH_TYPE_KEYWORD = "KEYWORD";
    /** 벡터 유사도 매칭 */
    public static final String MATCH_TYPE_VECTOR = "VECTOR";

    /** 검색된 저널 임베딩 엔티티 */
    private JournalEntryEmbeddingEntity entity;

    /** 검색 방식. KEYWORD 또는 VECTOR */
    private String matchType;

    /** 검색 점수. KEYWORD는 매칭 토큰 수, VECTOR는 가중치 적용 cosine score */
    private Double score;

    /** KEYWORD 검색에서 직접 매칭된 토큰 목록 */
    private List<String> matchedTokens;

    /** 로그/출처 표시용 짧은 본문 조각 */
    private String snippet;

    /**
     * 원본 저널 엔트리 ID를 반환한다.
     */
    public Integer getJournalEntryId() {
        return entity == null ? null : entity.getJournalEntryId();
    }
}
