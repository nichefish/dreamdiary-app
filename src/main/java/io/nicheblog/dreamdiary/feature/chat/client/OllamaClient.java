package io.nicheblog.dreamdiary.feature.chat.client;

import io.nicheblog.dreamdiary.feature.chat.model.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@Log4j2
public class OllamaClient {

    private static final String OLLAMA_URL = "http://localhost:11434/api/chat";
    private static final String MODEL = "qwen2.5:7b";

    private final RestTemplate restTemplate;

    public OllamaClient() {
        this.restTemplate = new RestTemplate();
    }

    public String chat(final String systemPrompt, final String userMessage) {
        return this.chat(systemPrompt, List.of(ChatMessageDto.builder()
                .role("USER")
                .content(userMessage)
                .build()));
    }

    public String chat(final String systemPrompt, final List<ChatMessageDto> contextMessages) {

        final OllamaChatRequest request = new OllamaChatRequest();

        request.setModel(MODEL);
        request.setStream(false);

        final List<OllamaChatRequest.Message> messages = new ArrayList<>();
        messages.add(new OllamaChatRequest.Message("system", systemPrompt));
        contextMessages.stream()
                .filter(message -> message.getContent() != null && !message.getContent().trim().isEmpty())
                .forEach(message -> messages.add(new OllamaChatRequest.Message(toOllamaRole(message.getRole()), message.getContent())));
        request.setMessages(messages);

        final HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<OllamaChatRequest> entity = new HttpEntity<>(request, headers);

        final ResponseEntity<OllamaChatResponse> response =
                restTemplate.exchange(
                        OLLAMA_URL,
                        HttpMethod.POST,
                        entity,
                        OllamaChatResponse.class
                );

        final OllamaChatResponse body = response.getBody();

        if (body == null || body.getMessage() == null || body.getMessage().getContent() == null) {
            throw new IllegalStateException("Ollama response is empty");
        }

        final String content = body.getMessage().getContent();
        log.info("Ollama response: {}", content);

        return content;
    }

    private String toOllamaRole(final String role) {
        if (role == null) return "user";

        final String normalized = role.toUpperCase(Locale.ROOT);
        if ("ASSISTANT".equals(normalized) || "AI".equals(normalized)) return "assistant";
        if ("SYSTEM".equals(normalized)) return "system";
        return "user";
    }
}
