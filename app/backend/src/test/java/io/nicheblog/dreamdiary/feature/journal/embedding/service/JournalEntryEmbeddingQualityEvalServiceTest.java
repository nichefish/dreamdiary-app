package io.nicheblog.dreamdiary.feature.journal.embedding.service;

import io.nicheblog.dreamdiary.feature.chat.client.OllamaClient;
import io.nicheblog.dreamdiary.feature.chat.model.OllamaHealthDto;
import io.nicheblog.dreamdiary.feature.journal.embedding.entity.JournalEntryEmbeddingEntity;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.JournalEntryEmbeddingQualityEvalReportDto;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.JournalEntryEmbeddingQualityEvalSuiteDto;
import io.nicheblog.dreamdiary.feature.journal.embedding.repository.jpa.JournalEntryEmbeddingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * {@link JournalEntryEmbeddingQualityEvalService} 단위 테스트.
 */
@ExtendWith(MockitoExtension.class)
class JournalEntryEmbeddingQualityEvalServiceTest {

    @Mock
    private JournalEntryEmbeddingRepository repository;

    @Mock
    private JournalEntryEmbeddingSearchService searchService;

    @Mock
    private OllamaClient ollamaClient;

    private JournalEntryEmbeddingQualityEvalService service;

    @BeforeEach
    void setUp() {
        service = new JournalEntryEmbeddingQualityEvalService(repository, searchService, ollamaClient);
        when(ollamaClient.getEmbeddingModel()).thenReturn("nomic-embed-text");
        when(ollamaClient.checkHealth()).thenReturn(healthyOllamaHealth());
        when(repository.countByEmbeddingStatus("EMBEDDED")).thenReturn(100L);
        when(repository.countByEmbeddingStatus("SKIPPED")).thenReturn(1L);
        when(repository.findAllByEmbeddingStatus("SKIPPED")).thenReturn(List.of(
                JournalEntryEmbeddingEntity.builder()
                        .journalEntryId(99)
                        .errorMessage("empty body")
                        .build()
        ));
        when(searchService.getCachedVectorCount()).thenReturn(2);
        when(searchService.sampleCachedJournalEntryIds(1)).thenReturn(List.of(1));
        when(searchService.getCachedVector(1)).thenReturn(Optional.of(new double[]{1.0D, 0.0D}));
    }

    /**
     * Ollama가 paraphrase를 구분 못 하면 REVIEW_MODEL 권고가 나와야 합니다.
     */
    @Test
    void runEval_shouldRecommendReviewWhenParaphraseSimilarityIsLow() {
        when(ollamaClient.embed(anyString())).thenReturn(List.of(0.0D, 1.0D));
        when(searchService.sampleCachedJournalEntryIds(12)).thenReturn(List.of());

        final JournalEntryEmbeddingQualityEvalReportDto report = service.runEval();

        assertFalse(report.isOverallPassed());
        assertEquals("REVIEW_MODEL", report.getRecommendation());
        assertTrue(report.getSuites().stream()
                .anyMatch(suite -> "PARAPHRASE".equals(suite.getCode()) && !suite.isSuitePassed()));
    }

    /**
     * paraphrase·distinct·corpus가 모두 통과하면 KEEP_MODEL이어야 합니다.
     */
    @Test
    void runEval_shouldRecommendKeepWhenAllSuitesPass() {
        when(ollamaClient.embed(anyString())).thenAnswer(invocation -> {
            final String text = invocation.getArgument(0, String.class);
            if (text.contains("꿈") || text.contains("바다")) {
                return List.of(1.0D, 0.0D, 0.0D);
            }
            if (text.contains("회의") || text.contains("길") || text.contains("오래")) {
                return List.of(0.9D, 0.1D, 0.0D);
            }
            if (text.contains("영화") || text.contains("점심 메뉴") || text.contains("고양이")) {
                return List.of(0.0D, 0.0D, 1.0D);
            }
            if (text.contains("충돌") || text.contains("말다툼") || text.contains("미팅")) {
                return List.of(0.8D, 0.2D, 0.0D);
            }
            return List.of(0.5D, 0.5D, 0.0D);
        });

        when(searchService.sampleCachedJournalEntryIds(12)).thenReturn(List.of(10));
        when(searchService.getCachedMeta(10)).thenReturn(Optional.of(
                JournalEntryEmbeddingEntity.builder()
                        .journalEntryId(10)
                        .embeddingText("본문: 꿈에서 바다를 걸었다")
                        .build()
        ));
        when(searchService.getCachedVector(10)).thenReturn(Optional.of(new double[]{1.0D, 0.0D, 0.0D}));

        final JournalEntryEmbeddingQualityEvalReportDto report = service.runEval();

        assertTrue(report.isOverallPassed());
        assertEquals("KEEP_MODEL", report.getRecommendation());
        assertEquals(3, report.getSuites().size());
        assertTrue(report.getSuites().stream().allMatch(JournalEntryEmbeddingQualityEvalSuiteDto::isSuitePassed));
    }

    /**
     * Ollama 연결 실패 시 OLLAMA_UNAVAILABLE이어야 합니다.
     */
    @Test
    void runEval_shouldReportOllamaUnavailable() {
        when(ollamaClient.checkHealth()).thenReturn(OllamaHealthDto.builder()
                .status("DOWN")
                .reachable(false)
                .baseUrl("http://localhost:11434")
                .chatModelRequired("qwen2.5:7b")
                .embeddingModelRequired("nomic-embed-text")
                .chatModelReady(false)
                .embeddingModelReady(false)
                .installedModels(List.of())
                .errorMessage("connection refused")
                .latencyMs(6L)
                .build());

        final JournalEntryEmbeddingQualityEvalReportDto report = service.runEval();

        assertFalse(report.isOverallPassed());
        assertEquals("OLLAMA_UNAVAILABLE", report.getRecommendation());
        assertTrue(report.getSuites().isEmpty());
        assertFalse(report.getOllamaHealth().isReachable());
        assertTrue(report.getSummary().contains("연결"));
    }

    private static OllamaHealthDto healthyOllamaHealth() {
        return OllamaHealthDto.builder()
                .status("UP")
                .reachable(true)
                .baseUrl("http://localhost:11434")
                .chatModelRequired("qwen2.5:7b")
                .embeddingModelRequired("nomic-embed-text")
                .chatModelReady(true)
                .embeddingModelReady(true)
                .installedModels(List.of("nomic-embed-text:latest", "qwen2.5:7b"))
                .latencyMs(3L)
                .build();
    }
}
