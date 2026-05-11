package io.nicheblog.dreamdiary.feature.journal.embedding.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JournalEntryEmbeddingStatsDto {

    private final long total;
    private final long pending;
    private final long processing;
    private final long embedded;
    private final long failed;
    private final long skipped;
    private final long remaining;
    private final long completed;
    private final double completionRate;
    private final double vectorizedRate;
}
