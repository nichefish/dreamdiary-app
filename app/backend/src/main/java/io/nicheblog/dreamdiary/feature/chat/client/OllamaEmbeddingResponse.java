package io.nicheblog.dreamdiary.feature.chat.client;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Ollama embeddings API 응답 payload입니다.
 */
@Getter
@Setter
public class OllamaEmbeddingResponse {

    /** 입력 텍스트를 표현하는 실수 벡터입니다. */
    private List<Double> embedding;
}
