package io.nicheblog.dreamdiary.feature.chat.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Ollama chat API에 전달하는 요청 payload입니다.
 */
@Getter
@Setter
public class OllamaChatRequest {

    /** 응답 생성에 사용할 Ollama 채팅 모델명입니다. */
    private String model;

    /** 스트리밍 응답 사용 여부입니다. 현재 화면 흐름에서는 동기 응답을 사용합니다. */
    private Boolean stream;

    /** 시스템 프롬프트와 대화 맥락을 순서대로 담은 메시지 목록입니다. */
    private List<Message> messages;

    /**
     * Ollama chat API가 요구하는 단일 메시지 항목입니다.
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message {

        /** {@code system}, {@code user}, {@code assistant} 중 하나의 역할명입니다. */
        private String role;

        /** 해당 역할이 전달하는 메시지 본문입니다. */
        private String content;
    }
}
