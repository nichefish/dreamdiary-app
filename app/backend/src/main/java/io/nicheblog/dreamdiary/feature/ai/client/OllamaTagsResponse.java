package io.nicheblog.dreamdiary.feature.ai.client;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Ollama {@code GET /api/tags} 응답 payload입니다.
 */
@Getter
@Setter
public class OllamaTagsResponse {

    private List<ModelSummary> models;

    @Getter
    @Setter
    public static class ModelSummary {
        private String name;
    }
}
