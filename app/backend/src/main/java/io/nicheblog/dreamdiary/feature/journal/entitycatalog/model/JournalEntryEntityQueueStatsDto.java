package io.nicheblog.dreamdiary.feature.journal.entitycatalog.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Admin-facing queue stats for asynchronous journal entity sync.
 */
@Getter
@Builder
public class JournalEntryEntityQueueStatsDto {

    /** Active journal entry count. Admin UI Total baseline. */
    private final long total;

    /** Queue row count in journal_entry_entity_job. */
    private final long queueRows;

    /** Entries without a queue row yet. */
    private final long unqueuedEntries;

    /** Pending rows waiting to be processed */
    private final long pending;

    /** Rows currently locked by a worker */
    private final long processing;

    /** Successfully synced rows */
    private final long synced;

    /** Failed rows */
    private final long failed;

    /** Rows skipped because the source hash was unchanged */
    private final long skipped;

    /** Rows not yet terminal */
    private final long remaining;

    /** Terminal rows */
    private final long completed;

    /** Entry coverage: synced / total active entries */
    private final double completionRate;

    /** Queue row terminal percentage among existing queue rows */
    private final double queueCompletionRate;
}
