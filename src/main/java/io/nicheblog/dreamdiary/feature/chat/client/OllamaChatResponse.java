package io.nicheblog.dreamdiary.feature.chat.client;

import lombok.Getter;
import lombok.Setter;

/**
 * Ollama chat API 응답 payload입니다.
 */
@Getter
@Setter
public class OllamaChatResponse {

    /** Ollama가 생성한 assistant 메시지입니다. */
    private Message message;

    /**
     * Ollama chat API 응답에 포함된 단일 메시지입니다.
     */
    @Getter
    @Setter
    public static class Message {

        /** 응답 메시지의 역할명입니다. */
        private String role;

        /** 응답 메시지 본문입니다. */
        private String content;
    }
}
