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

/**
 * 저널 엔트리 임베딩 작업 Entity.
 *
 * <p>원본 저널 엔트리의 의미상 시점, 컨텐츠 종류, 검색 가중치, 임베딩 텍스트,
 * 벡터 JSON과 처리 상태를 함께 보관한다.</p>
 */
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

    /** 임베딩 작업 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("임베딩 작업 ID")
    private Integer id;

    /** 원본 journal_entry ID */
    @Column(name = "journal_entry_id", nullable = false)
    @Comment("원본 journal_entry ID")
    private Integer journalEntryId;

    /** 원본 컨텐츠 타입. JOURNAL_DIARY, JOURNAL_DREAM, JOURNAL_NOTE 등 */
    @Column(name = "content_type", nullable = false, length = 50)
    @Comment("원본 컨텐츠 타입. JOURNAL_DIARY, JOURNAL_DREAM, JOURNAL_NOTE 등")
    private String contentType;

    /** 검색 가중치 분류. DIARY, DREAM, NOTE, UNKNOWN */
    @Column(name = "content_kind", nullable = false, length = 20)
    @Comment("검색 가중치 분류. DIARY, DREAM, NOTE, UNKNOWN")
    private String contentKind;

    /** 저널 기준 일자. 검색에서 의미상 시점으로 사용한다. */
    @Temporal(TemporalType.DATE)
    @Column(name = "journal_date")
    @Comment("저널 기준 일자. 검색에서 의미상 시점으로 사용")
    private Date journalDate;

    /** 저널 일자 정밀도. DAY, MONTH, YEAR, UNKNOWN 등 */
    @Column(name = "journal_date_precision", length = 20)
    @Comment("저널 일자 정밀도. DAY, MONTH, YEAR, UNKNOWN 등")
    private String journalDatePrecision;

    /** 검색 결과 랭킹에 곱할 타입별 가중치 */
    @Column(name = "retrieval_weight", precision = 5, scale = 2)
    @Comment("검색 결과 랭킹에 곱할 타입별 가중치")
    private BigDecimal retrievalWeight;

    /** 임베딩 처리 상태. PENDING, PROCESSING, EMBEDDED, FAILED, SKIPPED */
    @Column(name = "embedding_status", length = 20)
    @Comment("임베딩 처리 상태. PENDING, PROCESSING, EMBEDDED, FAILED, SKIPPED")
    private String embeddingStatus;

    /** 벡터를 생성한 임베딩 모델명 */
    @Column(name = "embedding_model", length = 100)
    @Comment("벡터를 생성한 임베딩 모델명")
    private String embeddingModel;

    /** 임베딩 모델에 실제로 전달하는 정규화된 텍스트 */
    @Lob
    @Column(name = "embedding_text", columnDefinition = "LONGTEXT")
    @Comment("임베딩 모델에 실제로 전달하는 정규화된 텍스트")
    private String embeddingText;

    /** 검색/스코어링/디버깅에 사용하는 구조화 메타데이터 JSON */
    @Lob
    @Column(name = "embedding_payload_json", columnDefinition = "LONGTEXT")
    @Comment("검색/스코어링/디버깅에 사용하는 구조화 메타데이터 JSON")
    private String embeddingPayloadJson;

    /** 임베딩 모델이 생성한 벡터 JSON 배열 */
    @Lob
    @Column(name = "embedding_vector_json", columnDefinition = "LONGTEXT")
    @Comment("임베딩 모델이 생성한 벡터 JSON 배열")
    private String embeddingVectorJson;

    /** embedding_text 기준 SHA-256 해시. 변경 감지 및 재임베딩 판단용 */
    @Column(name = "content_hash", length = 64)
    @Comment("embedding_text 기준 SHA-256 해시. 변경 감지 및 재임베딩 판단용")
    private String contentHash;

    /** 벡터 생성 완료 일시 */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "embedded_at")
    @Comment("벡터 생성 완료 일시")
    private Date embeddedAt;

    /** 임베딩 실패 또는 스킵 사유 */
    @Lob
    @Column(name = "error_message", columnDefinition = "LONGTEXT")
    @Comment("임베딩 실패 또는 스킵 사유")
    private String errorMessage;
}
