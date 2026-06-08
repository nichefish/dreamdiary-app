package io.nicheblog.dreamdiary.feature.journal.entitycatalog.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Result of requeueing journal entries into the entity sync queue.
 */
@Getter
@Builder
public class JournalEntryEntityQueueSyncResultDto {

    /** Current journal_entry row count */
    private final long activeEntryCount;

    /** Queue rows before sync */
    private final long queueCountBefore;

    /** Newly created queue rows */
    private final long created;

    /** Existing queue rows returned to pending */
    private final long requeued;

    /** Queue rows already aligned with the current content hash */
    private final long unchanged;

    /** Queue rows removed because the source entry disappeared */
    private final long removed;

    /** Queue rows after sync */
    private final long queueCountAfter;
}
