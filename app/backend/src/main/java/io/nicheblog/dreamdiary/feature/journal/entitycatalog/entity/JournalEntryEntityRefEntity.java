package io.nicheblog.dreamdiary.feature.journal.entitycatalog.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.type.JournalEntityMentionType;
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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Reference from a journal entry to a normalized journal entity.
 *
 * <p>The current phase stores direct person mentions first, but the table name
 * and columns stay entity-ready for later expansion.</p>
 */
@Entity
@Table(name = "journal_entry_entity_ref")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE journal_entry_entity_ref SET deleted_at = NOW() WHERE id = ?")
public class JournalEntryEntityRefEntity
        extends BaseAuditEntity {

    /** Journal entry entity reference ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("Journal entry entity reference ID")
    private Integer id;

    /** Referenced journal_entry ID */
    @Column(name = "journal_entry_id", nullable = false)
    @Comment("Referenced journal_entry ID")
    private Integer journalEntryId;

    /** Referenced journal_entity ID */
    @Column(name = "journal_entity_id", nullable = false)
    @Comment("Referenced journal_entity ID")
    private Integer journalEntityId;

    /** Original surface text in the entry */
    @Column(name = "surface_text", nullable = false, length = 200)
    @Comment("Original surface text in the entry")
    private String surfaceText;

    /** DIRECT, HONORIFIC, ALIAS, INFERRED */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "mention_type", nullable = false, length = 30)
    @Comment("DIRECT, HONORIFIC, ALIAS, INFERRED")
    private JournalEntityMentionType mentionType = JournalEntityMentionType.DIRECT;

    /** Evidence snippet for this mention */
    @Column(name = "evidence_snippet", columnDefinition = "TEXT")
    @Comment("Evidence snippet for this mention")
    private String evidenceSnippet;

    /** Extraction confidence */
    @Builder.Default
    @Column(name = "confidence", precision = 5, scale = 4)
    @Comment("Extraction confidence")
    private Double confidence = 1.0D;

    /** Optional start offset in source text */
    @Column(name = "start_offset")
    @Comment("Optional start offset in source text")
    private Integer startOffset;

    /** Optional end offset in source text */
    @Column(name = "end_offset")
    @Comment("Optional end offset in source text")
    private Integer endOffset;

    /** Mention order in the source entry */
    @Builder.Default
    @Column(name = "sort_order")
    @Comment("Mention order in the source entry")
    private Integer sortOrder = 1;
}
