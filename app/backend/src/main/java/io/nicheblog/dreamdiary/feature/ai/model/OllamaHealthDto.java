package io.nicheblog.dreamdiary.feature.ai.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Ollama 런타임 가용성 점검 결과.
 *
 * <p>Admin·Quality Eval에서 AI Chat/임베딩 워커가 의존하는 로컬 Ollama 상태를 표시합니다.</p>
 */
@Value
@Builder
public class OllamaHealthDto {

    /**
     * {@code UP}: 연결·필수 모델 준비, {@code DEGRADED}: 연결됐으나 필수 모델 누락, {@code DOWN}: 연결 불가.
     */
    String status;

    /** HTTP 연결 성공 여부 */
    boolean reachable;

    /** 점검에 사용한 Ollama base URL */
    String baseUrl;

    /** Chat에 필요한 모델명 */
    String chatModelRequired;

    /** 임베딩에 필요한 모델명 */
    String embeddingModelRequired;

    /** chat 모델 설치 여부 */
    boolean chatModelReady;

    /** embedding 모델 설치 여부 */
    boolean embeddingModelReady;

    /** 설치된 모델명 샘플 (최대 12개) */
    List<String> installedModels;

    /** 연결 실패·파싱 오류 메시지 */
    String errorMessage;

    /** /api/tags 응답까지 소요 ms */
    long latencyMs;
}
