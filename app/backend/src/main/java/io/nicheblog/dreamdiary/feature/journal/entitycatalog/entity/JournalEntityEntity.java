package io.nicheblog.dreamdiary.feature.journal.entitycatalog.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.type.JournalEntityType;
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
 * Journal entity catalog row.
 *
 * <p>This is intentionally entity-oriented even though the first extraction phase
 * only stores person entities.</p>
 */
@Entity
@Table(name = "journal_entity")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE journal_entity SET deleted_at = NOW() WHERE id = ?")
public class JournalEntityEntity
        extends BaseAuditEntity {

    /** Journal entity ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("Journal entity ID")
    private Integer id;

    /** Entity type. PERSON first, then EVENT/PLACE/ORG/SYMBOL later */
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 30)
    @Comment("Entity type. PERSON first, then EVENT/PLACE/ORG/SYMBOL later")
    private JournalEntityType entityType;

    /** Canonical display label */
    @Column(name = "canonical_label", nullable = false, length = 200)
    @Comment("Canonical display label")
    private String canonicalLabel;

    /** Normalized label for dedupe and lookup */
    @Column(name = "normalized_label", nullable = false, length = 200)
    @Comment("Normalized label for dedupe and lookup")
    private String normalizedLabel;

    /** ACTIVE, MERGED, IGNORED */
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    @Comment("ACTIVE, MERGED, IGNORED")
    private String status = "ACTIVE";
}
