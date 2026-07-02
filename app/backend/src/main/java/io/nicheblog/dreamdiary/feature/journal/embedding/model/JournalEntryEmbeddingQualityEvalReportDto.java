package io.nicheblog.dreamdiary.feature.journal.embedding.model;

import io.nicheblog.dreamdiary.feature.chat.model.OllamaHealthDto;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * 임베딩 품질 실측 전체 리포트.
 *
 * <p>한국어 의미 유사도가 현재 모델에서 RAG에 쓸 만한지 빠르게 판단하기 위한 운영 도구 결과입니다.</p>
 */
@Value
@Builder
public class JournalEntryEmbeddingQualityEvalReportDto {

    /** 현재 Ollama 임베딩 모델명 */
    String embeddingModel;

    /** EMBEDDED 상태 row 수 */
    long embeddedCount;

    /** 메모리 캐시에 로드된 벡터 수 */
    int cachedVectorCount;

    /** 벡터 차원 (첫 샘플 기준, 없으면 null) */
    Integer vectorDimension;

    /** SKIPPED row 수 */
    long skippedCount;

    /** SKIPPED 사유 샘플 (최대 20건) */
    List<JournalEntryEmbeddingSkippedSampleDto> skippedSamples;

    /** 실측 시점 Ollama health (선행 핑) */
    OllamaHealthDto ollamaHealth;

    /** 서브 스위트 결과 */
    List<JournalEntryEmbeddingQualityEvalSuiteDto> suites;

    /** 전체 통과 여부 */
    boolean overallPassed;

    /**
     * 권고: KEEP_MODEL(유지), REVIEW_MODEL(교체 검토), OLLAMA_UNAVAILABLE(실측 불가).
     */
    String recommendation;

    /** 한 줄 요약 */
    String summary;

    /** 실측 소요 ms */
    long elapsedMs;
}
