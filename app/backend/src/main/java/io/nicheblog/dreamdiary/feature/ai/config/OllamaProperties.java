package io.nicheblog.dreamdiary.feature.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 로컬 Ollama 연결·모델명 설정.
 *
 * <p>{@code application.yml}의 {@code app.ollama.*}와 프로필별 오버라이드
 * ({@code application-local.yml} 등)로 chat/embedding 모델을 바꿀 수 있습니다.</p>
 */
@Component
@ConfigurationProperties(prefix = "app.ollama")
@Getter
@Setter
public class OllamaProperties {

    /** Ollama API base URL (trailing slash 없음 권장) */
    private String baseUrl = "http://localhost:11434";

    /** Chat API ({@code /api/chat})에 사용할 모델 태그 */
    private String chatModel = "qwen2.5:7b";

    /** Embeddings API ({@code /api/embeddings})에 사용할 모델 태그 */
    private String embeddingModel = "nomic-embed-text";

    /** Chat 생성 temperature (null이면 Ollama 기본값) */
    private Double chatTemperature = 0.35D;

    /** Chat 최대 생성 토큰 수 (null이면 Ollama 기본값) */
    private Integer numPredict = 768;

    /** HTTP connect timeout (ms) */
    private int connectTimeoutMs = 5_000;

    /** HTTP read timeout (ms). 로컬 14B 응답 대기를 고려해 기본 5분 */
    private int readTimeoutMs = 300_000;
}
