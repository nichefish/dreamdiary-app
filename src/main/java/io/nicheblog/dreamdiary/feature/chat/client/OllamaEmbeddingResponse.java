package io.nicheblog.dreamdiary.feature.chat.client;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OllamaEmbeddingResponse {

    private List<Double> embedding;
}
