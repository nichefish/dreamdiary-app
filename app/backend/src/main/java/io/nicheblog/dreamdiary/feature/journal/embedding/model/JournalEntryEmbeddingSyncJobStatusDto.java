package io.nicheblog.dreamdiary.feature.journal.embedding.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Getter
@Builder
public class JournalEntryEmbeddingSyncJobStatusDto {

    private final boolean running;
    private final String phase;
    private final Date startedAt;
    private final Date finishedAt;
    private final long processed;
    private final long total;
    private final JournalEntryEmbeddingSyncResultDto result;
    private final String errorMessage;
}
