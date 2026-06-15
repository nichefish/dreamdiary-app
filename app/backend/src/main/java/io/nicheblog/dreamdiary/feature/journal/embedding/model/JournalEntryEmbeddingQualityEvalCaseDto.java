package io.nicheblog.dreamdiary.feature.journal.embedding.model;

import lombok.Builder;
import lombok.Value;

/**
 * 임베딩 품질 실측 단일 케이스 결과.
 */
@Value
@Builder
public class JournalEntryEmbeddingQualityEvalCaseDto {

    /** 케이스 식별자 (예: paraphrase-01, rank-entry-123) */
    String caseId;

    /** 사람이 읽을 수 있는 설명 */
    String description;

    /** 기대 조건 요약 */
    String expectation;

    /** 통과 여부 */
    boolean passed;

    /** 측정값 (유사도, 마진 등) */
    Double metric;

    /** 보조 측정값 (비교 대상 유사도 등) */
    Double comparisonMetric;

    /** 실패 시 원인 */
    String detail;
}
