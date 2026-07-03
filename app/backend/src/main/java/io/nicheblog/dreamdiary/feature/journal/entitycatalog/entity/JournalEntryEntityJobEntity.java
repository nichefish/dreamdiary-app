package io.nicheblog.dreamdiary.feature.journal.entitycatalog.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.time.LocalDateTime;

/**
 * Queue row for asynchronous journal entity-ref and role sync.
 *
 * <p>Entity mentions and mention roles must stay on the same freshness boundary,
 * so this job represents both ref and role regeneration for one journal entry.</p>
 */
@Entity
@Table(name = "journal_entry_entity_job")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE journal_entry_entity_job SET deleted_at = NOW() WHERE id = ?")
public class JournalEntryEntityJobEntity
        extends BaseAuditEntity {

    /** Queue row ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("Queue row ID")
    private Integer id;

    /** Source journal_entry ID */
    @Column(name = "journal_entry_id", nullable = false)
    @Comment("Source journal_entry ID")
    private Integer journalEntryId;

    /** Sync status. PENDING, PROCESSING, SYNCED, FAILED, SKIPPED */
    @Builder.Default
    @Column(name = "job_status", nullable = false, length = 20)
    @Comment("Sync status. PENDING, PROCESSING, SYNCED, FAILED, SKIPPED")
    private String jobStatus = "PENDING";

    /** Content hash used to skip unchanged rows */
    @Column(name = "content_hash", length = 64)
    @Comment("Content hash used to skip unchanged rows")
    private String contentHash;

    /** Worker node name when the row is currently processing */
    @Column(name = "locked_by", length = 200)
    @Comment("Worker node name when the row is currently processing")
    private String lockedBy;

    /** Time when the queue row was last fully processed */
    @Column(name = "processed_at")
    @Comment("Time when the queue row was last fully processed")
    private LocalDateTime processedAt;

    /** Last worker error, if any */
    @Lob
    @Column(name = "error_message", columnDefinition = "LONGTEXT")
    @Comment("Last worker error, if any")
    private String errorMessage;
}
