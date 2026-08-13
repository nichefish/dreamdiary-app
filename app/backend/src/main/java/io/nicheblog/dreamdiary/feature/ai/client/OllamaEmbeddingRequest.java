package io.nicheblog.dreamdiary.feature.ai.client;

import lombok.Getter;
import lombok.Setter;

/**
 * Ollama embeddings API에 전달하는 요청 payload입니다.
 */
@Getter
@Setter
public class OllamaEmbeddingRequest {

    /** 임베딩 생성에 사용할 Ollama 모델명입니다. */
    private String model;

    /** 벡터화할 원문 텍스트입니다. */
    private String prompt;
}
