package io.nicheblog.dreamdiary.feature.journal.embedding.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "journal_entry_embedding_sync_job")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE journal_entry_embedding_sync_job SET deleted_at = NOW() WHERE id = ?")
public class JournalEntryEmbeddingSyncJobEntity
        extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("임베딩 sync job ID")
    private Integer id;

    @Column(name = "job_key", nullable = false, unique = true, length = 100)
    @Comment("job 식별 키")
    private String jobKey;

    @Column(name = "status", nullable = false, length = 20)
    @Comment("IDLE, RUNNING, COMPLETED, FAILED")
    private String status;

    @Column(name = "phase", nullable = false, length = 30)
    @Comment("현재 처리 단계")
    private String phase;

    @Column(name = "processed_count")
    @Comment("처리한 entry 수")
    private Long processedCount;

    @Column(name = "total_count")
    @Comment("처리 대상 entry 수")
    private Long totalCount;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "started_at")
    @Comment("작업 시작 일시")
    private Date startedAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "finished_at")
    @Comment("작업 종료 일시")
    private Date finishedAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "heartbeat_at")
    @Comment("작업 heartbeat 일시")
    private Date heartbeatAt;

    @Column(name = "locked_by", length = 120)
    @Comment("작업 실행 노드")
    private String lockedBy;

    @Lob
    @Column(name = "result_json", columnDefinition = "LONGTEXT")
    @Comment("작업 결과 JSON")
    private String resultJson;

    @Lob
    @Column(name = "error_message", columnDefinition = "LONGTEXT")
    @Comment("작업 오류 메시지")
    private String errorMessage;
}
