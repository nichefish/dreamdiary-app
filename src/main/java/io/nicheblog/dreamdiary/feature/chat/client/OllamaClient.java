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

/**
 * 로컬 Ollama 서버와 통신하는 채팅/임베딩 클라이언트입니다.
 *
 * <p>애플리케이션 내부의 채팅 메시지 표현을 Ollama API payload로 변환하고,
 * 채팅 응답과 임베딩 벡터를 호출자에게 단순한 값으로 돌려줍니다.</p>
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class OllamaClient {

    private static final String OLLAMA_CHAT_URL = "http://localhost:11434/api/chat";
    private static final String OLLAMA_EMBEDDING_URL = "http://localhost:11434/api/embeddings";
    private static final String CHAT_MODEL = "qwen2.5:7b";
    private static final String EMBEDDING_MODEL = "nomic-embed-text";

    private final RestTemplate restTemplate;

    /**
     * 기본 {@link RestTemplate} 인스턴스로 Ollama 클라이언트를 생성합니다.
     */
    public OllamaClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 시스템 프롬프트와 단일 사용자 메시지로 AI 응답을 생성합니다.
     *
     * @param systemPrompt AI에게 적용할 시스템 지시문
     * @param userMessage 사용자 입력 메시지
     * @return Ollama가 생성한 assistant 응답 본문
     */
    public String chat(final String systemPrompt, final String userMessage) {
        return this.chat(systemPrompt, List.of(ChatMessageDto.builder()
                .role("USER")
                .content(userMessage)
                .build()));
    }

    /**
     * 시스템 프롬프트와 대화 맥락을 Ollama chat API에 전달해 AI 응답을 생성합니다.
     *
     * @param systemPrompt AI에게 적용할 시스템 지시문
     * @param contextMessages 최근 대화 맥락과 현재 사용자 메시지 목록
     * @return Ollama가 생성한 assistant 응답 본문
     * @throws IllegalStateException Ollama 응답 본문이 비어 있을 때
     */
    public String chat(final String systemPrompt, final List<ChatMessageDto> contextMessages) {

        final OllamaChatRequest request = new OllamaChatRequest();

        request.setModel(CHAT_MODEL);
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
                        OLLAMA_CHAT_URL,
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

    /**
     * 입력 텍스트를 임베딩 모델로 벡터화합니다.
     *
     * @param text 벡터로 변환할 텍스트
     * @return Ollama가 반환한 임베딩 벡터
     * @throws IllegalStateException Ollama 임베딩 응답이 비어 있을 때
     */
    public List<Double> embed(final String text) {
        final OllamaEmbeddingRequest request = new OllamaEmbeddingRequest();
        request.setModel(EMBEDDING_MODEL);
        request.setPrompt(text);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<OllamaEmbeddingRequest> entity = new HttpEntity<>(request, headers);

        final ResponseEntity<OllamaEmbeddingResponse> response =
                restTemplate.exchange(
                        OLLAMA_EMBEDDING_URL,
                        HttpMethod.POST,
                        entity,
                        OllamaEmbeddingResponse.class
                );

        final OllamaEmbeddingResponse body = response.getBody();

        if (body == null || body.getEmbedding() == null || body.getEmbedding().isEmpty()) {
            throw new IllegalStateException("Ollama embedding response is empty");
        }

        return body.getEmbedding();
    }

    /**
     * 현재 임베딩 생성에 사용하는 Ollama 모델명을 반환합니다.
     *
     * @return 임베딩 모델명
     */
    public String getEmbeddingModel() {
        return EMBEDDING_MODEL;
    }

    /**
     * 애플리케이션 내부 역할명을 Ollama chat API 역할명으로 정규화합니다.
     *
     * @param role 애플리케이션 내부 메시지 역할명
     * @return Ollama API가 인식하는 역할명
     */
    private String toOllamaRole(final String role) {
        if (role == null) return "user";

        final String normalized = role.toUpperCase(Locale.ROOT);
        if ("ASSISTANT".equals(normalized) || "AI".equals(normalized)) return "assistant";
        if ("SYSTEM".equals(normalized)) return "system";
        return "user";
    }
}
