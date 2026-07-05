package io.nicheblog.dreamdiary.feature.journal.embedding.model;

import lombok.Builder;
import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Getter
@Builder
public class JournalEntryEmbeddingSyncJobStatusDto {

    private final boolean running;
    private final String phase;
    /** LocalDateTime 전환 후에도 API 직렬화 포맷(yyyy-MM-dd HH:mm:ss) 계약 유지 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime startedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime finishedAt;
    private final long processed;
    private final long total;
    private final JournalEntryEmbeddingSyncResultDto result;
    private final String errorMessage;
}
