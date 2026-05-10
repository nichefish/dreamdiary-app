package io.nicheblog.dreamdiary.feature.chat.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

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

        final OllamaChatRequest request = new OllamaChatRequest();

        request.setModel(MODEL);
        request.setStream(false);

        request.setMessages(List.of(
                new OllamaChatRequest.Message("system", systemPrompt),
                new OllamaChatRequest.Message("user", userMessage)
        ));

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
}
