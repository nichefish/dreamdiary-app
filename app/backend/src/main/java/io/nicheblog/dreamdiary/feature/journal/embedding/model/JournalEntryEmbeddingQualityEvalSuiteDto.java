package io.nicheblog.dreamdiary.feature.journal.embedding.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * 임베딩 품질 실측 서브 스위트 결과.
 */
@Value
@Builder
public class JournalEntryEmbeddingQualityEvalSuiteDto {

    /** 스위트 코드 (PARAPHRASE, RELATED_DISTINCT, CORPUS_SELF_RANK) */
    String code;

    /** 스위트 설명 */
    String description;

    /** 통과 기준 요약 */
    String passCriteria;

    /** 통과한 케이스 수 */
    int passedCount;

    /** 실패한 케이스 수 */
    int failedCount;

    /** 스위트 전체 통과 여부 */
    boolean suitePassed;

    /** 개별 케이스 결과 */
    List<JournalEntryEmbeddingQualityEvalCaseDto> cases;
}
