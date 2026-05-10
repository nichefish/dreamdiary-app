package io.nicheblog.dreamdiary.feature.chat.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OllamaChatResponse {

    private Message message;

    @Getter
    @Setter
    public static class Message {
        private String role;
        private String content;
    }
}
