package io.nicheblog.dreamdiary.feature.ai.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link OllamaProperties} 바인딩 테스트.
 */
@SpringBootTest(classes = OllamaProperties.class)
@EnableConfigurationProperties(OllamaProperties.class)
@TestPropertySource(properties = {
        "app.ollama.base-url=http://127.0.0.1:11435",
        "app.ollama.chat-model=qwen2.5:14b",
        "app.ollama.embedding-model=nomic-embed-text",
        "app.ollama.chat-temperature=0.35",
        "app.ollama.num-predict=768",
        "app.ollama.connect-timeout-ms=5000",
        "app.ollama.read-timeout-ms=300000"
})
class OllamaPropertiesTest {

    @Autowired
    private OllamaProperties ollamaProperties;

    @Test
    void shouldBindOllamaPropertiesFromApplicationConfig() {
        assertEquals("http://127.0.0.1:11435", ollamaProperties.getBaseUrl());
        assertEquals("qwen2.5:14b", ollamaProperties.getChatModel());
        assertEquals("nomic-embed-text", ollamaProperties.getEmbeddingModel());
        assertEquals(0.35D, ollamaProperties.getChatTemperature());
        assertEquals(768, ollamaProperties.getNumPredict());
        assertEquals(5_000, ollamaProperties.getConnectTimeoutMs());
        assertEquals(300_000, ollamaProperties.getReadTimeoutMs());
    }
}
