package io.nicheblog.dreamdiary.feature.chat.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OllamaEmbeddingRequest {

    private String model;
    private String prompt;
}
