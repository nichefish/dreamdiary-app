package io.nicheblog.dreamdiary.feature.journal.entitycatalog.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.type.JournalEntityRoleType;
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
 * Role/function evidence derived from one journal entry entity mention.
 *
 * <p>This stores per-mention interpretation signals instead of a merged entity-level
 * truth so later re-aggregation can stay reversible.</p>
 */
@Entity
@Table(name = "journal_entry_entity_role")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE journal_entry_entity_role SET deleted_at = NOW() WHERE id = ?")
public class JournalEntryEntityRoleEntity
        extends BaseAuditEntity {

    /** Journal entry entity role ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("Journal entry entity role ID")
    private Integer id;

    /** Referenced journal_entry_entity_ref ID */
    @Column(name = "journal_entry_entity_ref_id", nullable = false)
    @Comment("Referenced journal_entry_entity_ref ID")
    private Integer journalEntryEntityRefId;

    /** COLLABORATION, TENSION, EVALUATION, CARE, CONFLICT, DESIRE, SYMBOLIC_FIGURE, UNKNOWN */
    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, length = 40)
    @Comment("COLLABORATION, TENSION, EVALUATION, CARE, CONFLICT, DESIRE, SYMBOLIC_FIGURE, UNKNOWN")
    private JournalEntityRoleType roleType;

    /** Evidence snippet for this role judgment */
    @Column(name = "evidence_snippet", columnDefinition = "TEXT")
    @Comment("Evidence snippet for this role judgment")
    private String evidenceSnippet;

    /** Role extraction confidence */
    @Builder.Default
    @Column(name = "confidence", precision = 5, scale = 4)
    @Comment("Role extraction confidence")
    private Double confidence = 1.0D;
}
