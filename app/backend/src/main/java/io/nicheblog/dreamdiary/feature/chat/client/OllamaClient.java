package io.nicheblog.dreamdiary.feature.chat.client;

import io.nicheblog.dreamdiary.feature.chat.config.OllamaProperties;
import io.nicheblog.dreamdiary.feature.chat.model.ChatMessageDto;
import io.nicheblog.dreamdiary.feature.chat.model.OllamaHealthDto;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 로컬 Ollama 서버와 통신하는 채팅/임베딩 클라이언트입니다.
 *
 * <p>애플리케이션 내부의 채팅 메시지 표현을 Ollama API payload로 변환하고,
 * 채팅 응답과 임베딩 벡터를 호출자에게 단순한 값으로 돌려줍니다.</p>
 */
@Component
@Log4j2
public class OllamaClient {

    private static final int INSTALLED_MODEL_SAMPLE_LIMIT = 12;

    private final OllamaProperties ollamaProperties;
    private final RestTemplate restTemplate;

    /**
     * @param ollamaProperties {@code app.ollama.*} 설정
     */
    public OllamaClient(final OllamaProperties ollamaProperties) {
        this.ollamaProperties = ollamaProperties;
        this.restTemplate = createRestTemplate(ollamaProperties);
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

        final long start = System.currentTimeMillis();
        final OllamaChatRequest request = new OllamaChatRequest();

        request.setModel(getChatModel());
        request.setStream(false);
        request.setOptions(buildChatOptions());

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
                        resolveApiUrl("/api/chat"),
                        HttpMethod.POST,
                        entity,
                        OllamaChatResponse.class
                );

        final OllamaChatResponse body = response.getBody();

        if (body == null || body.getMessage() == null || body.getMessage().getContent() == null) {
            throw new IllegalStateException("Ollama response is empty");
        }

        final String content = body.getMessage().getContent();
        log.info("Ollama chat completed. model={}, latencyMs={}, responseChars={}",
                getChatModel(),
                System.currentTimeMillis() - start,
                StringUtils.length(content));

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
        request.setModel(getEmbeddingModel());
        request.setPrompt(text);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<OllamaEmbeddingRequest> entity = new HttpEntity<>(request, headers);

        final ResponseEntity<OllamaEmbeddingResponse> response =
                restTemplate.exchange(
                        resolveApiUrl("/api/embeddings"),
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
     * Ollama {@code /api/tags}로 런타임 가용성을 점검합니다.
     *
     * <p>자동 기동은 하지 않으며, 연결·필수 모델 설치 여부만 반환합니다.</p>
     *
     * @return Ollama health DTO
     */
    public OllamaHealthDto checkHealth() {
        final long start = System.currentTimeMillis();
        final String baseUrl = getBaseUrl();
        final String chatModel = getChatModel();
        final String embeddingModel = getEmbeddingModel();
        try {
            final ResponseEntity<OllamaTagsResponse> response =
                    restTemplate.getForEntity(resolveApiUrl("/api/tags"), OllamaTagsResponse.class);
            final OllamaTagsResponse body = response.getBody();
            final List<String> installedModels = extractInstalledModelNames(body);
            final boolean chatModelReady = hasInstalledModel(installedModels, chatModel);
            final boolean embeddingModelReady = hasInstalledModel(installedModels, embeddingModel);
            final String status = resolveHealthStatus(true, chatModelReady, embeddingModelReady);
            return OllamaHealthDto.builder()
                    .status(status)
                    .reachable(true)
                    .baseUrl(baseUrl)
                    .chatModelRequired(chatModel)
                    .embeddingModelRequired(embeddingModel)
                    .chatModelReady(chatModelReady)
                    .embeddingModelReady(embeddingModelReady)
                    .installedModels(installedModels)
                    .errorMessage(buildHealthWarningMessage(chatModel, embeddingModel, chatModelReady, embeddingModelReady))
                    .latencyMs(System.currentTimeMillis() - start)
                    .build();
        } catch (final RestClientException exception) {
            log.warn("Ollama health check failed. baseUrl={}, error={}", baseUrl, exception.getMessage());
            return OllamaHealthDto.builder()
                    .status("DOWN")
                    .reachable(false)
                    .baseUrl(baseUrl)
                    .chatModelRequired(chatModel)
                    .embeddingModelRequired(embeddingModel)
                    .chatModelReady(false)
                    .embeddingModelReady(false)
                    .installedModels(List.of())
                    .errorMessage(StringUtils.abbreviate(StringUtils.defaultString(exception.getMessage()), 240))
                    .latencyMs(System.currentTimeMillis() - start)
                    .build();
        }
    }

    /**
     * 임베딩·품질 실측에 Ollama가 준비됐는지 확인합니다.
     */
    public boolean isReadyForEmbedding() {
        final OllamaHealthDto health = checkHealth();
        return health.isReachable() && health.isEmbeddingModelReady();
    }

    /**
     * 현재 임베딩 생성에 사용하는 Ollama 모델명을 반환합니다.
     *
     * @return 임베딩 모델명
     */
    public String getEmbeddingModel() {
        return StringUtils.defaultIfBlank(ollamaProperties.getEmbeddingModel(), "nomic-embed-text");
    }

    /**
     * 현재 chat에 사용하는 Ollama 모델명을 반환합니다.
     */
    public String getChatModel() {
        return StringUtils.defaultIfBlank(ollamaProperties.getChatModel(), "qwen2.5:7b");
    }

    /**
     * Ollama base URL을 반환합니다.
     */
    public String getBaseUrl() {
        return StringUtils.defaultIfBlank(ollamaProperties.getBaseUrl(), "http://localhost:11434");
    }

    /**
     * base URL과 API path를 합쳐 호출 URL을 만듭니다.
     */
    private String resolveApiUrl(final String path) {
        final String normalizedPath = StringUtils.defaultIfBlank(path, "");
        if (!normalizedPath.startsWith("/")) {
            throw new IllegalArgumentException("Ollama API path must start with '/': " + path);
        }
        return StringUtils.stripEnd(getBaseUrl(), "/") + normalizedPath;
    }

    private RestTemplate createRestTemplate(final OllamaProperties properties) {
        final SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeoutMs());
        factory.setReadTimeout(properties.getReadTimeoutMs());
        return new RestTemplate(factory);
    }

    private Map<String, Object> buildChatOptions() {
        final Map<String, Object> options = new LinkedHashMap<>();
        if (ollamaProperties.getChatTemperature() != null) {
            options.put("temperature", ollamaProperties.getChatTemperature());
        }
        if (ollamaProperties.getNumPredict() != null) {
            options.put("num_predict", ollamaProperties.getNumPredict());
        }
        return options.isEmpty() ? null : options;
    }

    private List<String> extractInstalledModelNames(final OllamaTagsResponse body) {
        if (body == null || body.getModels() == null) return List.of();
        return body.getModels().stream()
                .map(OllamaTagsResponse.ModelSummary::getName)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .limit(INSTALLED_MODEL_SAMPLE_LIMIT)
                .collect(Collectors.toList());
    }

    private boolean hasInstalledModel(final List<String> installedModels, final String requiredModel) {
        if (installedModels == null || installedModels.isEmpty() || StringUtils.isBlank(requiredModel)) {
            return false;
        }
        return installedModels.stream().anyMatch(name -> modelNameMatches(name, requiredModel));
    }

    private boolean modelNameMatches(final String installedName, final String requiredModel) {
        if (StringUtils.isBlank(installedName) || StringUtils.isBlank(requiredModel)) return false;
        final String normalizedInstalled = StringUtils.trim(installedName);
        final String normalizedRequired = StringUtils.trim(requiredModel);
        if (StringUtils.equalsIgnoreCase(normalizedInstalled, normalizedRequired)) return true;
        return StringUtils.startsWithIgnoreCase(normalizedInstalled, normalizedRequired + ":");
    }

    private String resolveHealthStatus(
            final boolean reachable,
            final boolean chatModelReady,
            final boolean embeddingModelReady
    ) {
        if (!reachable) return "DOWN";
        if (chatModelReady && embeddingModelReady) return "UP";
        return "DEGRADED";
    }

    private String buildHealthWarningMessage(
            final String chatModel,
            final String embeddingModel,
            final boolean chatModelReady,
            final boolean embeddingModelReady
    ) {
        if (chatModelReady && embeddingModelReady) return null;
        final List<String> missing = new ArrayList<>();
        if (!chatModelReady) missing.add(chatModel);
        if (!embeddingModelReady) missing.add(embeddingModel);
        return "missing models: " + String.join(", ", missing);
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
