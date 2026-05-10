package io.nicheblog.dreamdiary.feature.chat.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OllamaChatRequest {

    private String model;
    private Boolean stream;
    private List<Message> messages;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}
