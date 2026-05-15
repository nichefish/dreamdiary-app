package io.nicheblog.dreamdiary.feature.journal.embedding.service;

import io.nicheblog.dreamdiary.feature.chat.client.OllamaClient;
import io.nicheblog.dreamdiary.feature.journal.embedding.entity.JournalEntryEmbeddingEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JournalEntryEmbeddingWorkerTest {

    @Mock
    private JournalEntryEmbeddingQueueService queueService;

    @Mock
    private JournalEntryEmbeddingSearchService searchService;

    @Mock
    private OllamaClient ollamaClient;

    private JournalEntryEmbeddingWorker worker;

    @BeforeEach
    void setUp() {
        worker = new JournalEntryEmbeddingWorker(queueService, searchService, ollamaClient);
        when(queueService.countPending()).thenReturn(0L);
    }

    @Test
    void processPendingBatch_marksContextLengthErrorFailedAndContinues() {
        final JournalEntryEmbeddingEntity tooLong = entity(1, 101, "hash-1", "too long");
        final JournalEntryEmbeddingEntity next = entity(2, 102, "hash-2", "next");

        when(queueService.claimPendingBatch(20)).thenReturn(List.of(tooLong, next));
        when(ollamaClient.embed("too long")).thenThrow(contextLengthException());
        when(ollamaClient.embed("next")).thenReturn(List.of(1.0D, 2.0D));
        when(ollamaClient.getEmbeddingModel()).thenReturn("nomic-embed-text");

        final int successCount = worker.processPendingBatch(20);

        assertEquals(1, successCount);
        verify(queueService).markFailed(eq(1), argThat(exception ->
                exception.getMessage().contains("exceeds context length")));
        verify(queueService).markEmbedded(eq(2), eq("hash-2"), eq("nomic-embed-text"), eq("[1.0,2.0]"));
        verify(searchService).refreshEntry(102);
        verify(queueService, never()).requeueByIds(anyList(), anyString());
    }

    @Test
    void processPendingBatch_chunksLongTextAndStoresAverageVector() {
        final String longText = "a".repeat(2000);
        final JournalEntryEmbeddingEntity entity = entity(1, 101, "hash-1", longText);

        when(queueService.claimPendingBatch(20)).thenReturn(List.of(entity));
        when(ollamaClient.embed(anyString()))
                .thenReturn(List.of(2.0D, 4.0D))
                .thenReturn(List.of(4.0D, 8.0D));
        when(ollamaClient.getEmbeddingModel()).thenReturn("nomic-embed-text");

        final int successCount = worker.processPendingBatch(20);

        assertEquals(1, successCount);

        final ArgumentCaptor<String> chunkCaptor = ArgumentCaptor.forClass(String.class);
        verify(ollamaClient, org.mockito.Mockito.times(2)).embed(chunkCaptor.capture());
        assertTrue(chunkCaptor.getAllValues().stream().allMatch(chunk -> chunk.length() <= 1600));
        verify(queueService).markEmbedded(eq(1), eq("hash-1"), eq("nomic-embed-text"), eq("[3.0,6.0]"));
    }

    private JournalEntryEmbeddingEntity entity(
            final Integer id,
            final Integer journalEntryId,
            final String contentHash,
            final String embeddingText
    ) {
        final JournalEntryEmbeddingEntity entity = new JournalEntryEmbeddingEntity();
        entity.setId(id);
        entity.setJournalEntryId(journalEntryId);
        entity.setContentHash(contentHash);
        entity.setEmbeddingText(embeddingText);
        return entity;
    }

    private HttpServerErrorException contextLengthException() {
        return HttpServerErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                HttpHeaders.EMPTY,
                "{\"error\":\"the input length exceeds the context length\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );
    }
}
