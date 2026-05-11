package io.nicheblog.dreamdiary.feature.journal.embedding.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "journal_entry_embedding")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE journal_entry_embedding SET deleted_at = NOW() WHERE id = ?")
public class JournalEntryEmbeddingEntity
        extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("embedding id")
    private Integer id;

    @Column(name = "journal_entry_id", nullable = false)
    @Comment("journal_entry.id")
    private Integer journalEntryId;

    @Column(name = "content_type", nullable = false, length = 50)
    @Comment("JOURNAL_DIARY | JOURNAL_DREAM | JOURNAL_NOTE")
    private String contentType;

    @Column(name = "content_kind", nullable = false, length = 20)
    @Comment("DIARY | DREAM | NOTE | UNKNOWN")
    private String contentKind;

    @Temporal(TemporalType.DATE)
    @Column(name = "journal_date")
    @Comment("semantic journal date")
    private Date journalDate;

    @Column(name = "journal_date_precision", length = 20)
    @Comment("DAY | MONTH | YEAR | UNKNOWN")
    private String journalDatePrecision;

    @Column(name = "retrieval_weight", precision = 5, scale = 2)
    @Comment("retrieval scoring weight")
    private BigDecimal retrievalWeight;

    @Column(name = "embedding_status", length = 20)
    @Comment("PENDING | PROCESSING | EMBEDDED | FAILED | SKIPPED")
    private String embeddingStatus;

    @Column(name = "embedding_model", length = 100)
    @Comment("embedding model")
    private String embeddingModel;

    @Lob
    @Column(name = "embedding_text", columnDefinition = "LONGTEXT")
    @Comment("actual text sent to embedding model")
    private String embeddingText;

    @Lob
    @Column(name = "embedding_payload_json", columnDefinition = "LONGTEXT")
    @Comment("structured embedding metadata JSON")
    private String embeddingPayloadJson;

    @Lob
    @Column(name = "embedding_vector_json", columnDefinition = "LONGTEXT")
    @Comment("embedding vector JSON payload")
    private String embeddingVectorJson;

    @Column(name = "content_hash", length = 64)
    @Comment("SHA-256 hash of embedding_text")
    private String contentHash;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "embedded_at")
    @Comment("embedding completed at")
    private Date embeddedAt;

    @Lob
    @Column(name = "error_message", columnDefinition = "LONGTEXT")
    @Comment("embedding error message")
    private String errorMessage;
}
