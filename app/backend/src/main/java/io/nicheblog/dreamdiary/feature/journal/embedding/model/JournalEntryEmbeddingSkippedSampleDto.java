package io.nicheblog.dreamdiary.feature.journal.embedding.model;

import lombok.Builder;
import lombok.Value;

/**
 * SKIPPED 임베딩 row 샘플 (운영 진단용).
 */
@Value
@Builder
public class JournalEntryEmbeddingSkippedSampleDto {

    Integer journalEntryId;
    String errorMessage;
}
