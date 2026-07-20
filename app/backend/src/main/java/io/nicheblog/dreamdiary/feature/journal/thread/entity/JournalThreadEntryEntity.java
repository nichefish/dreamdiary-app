package io.nicheblog.dreamdiary.feature.journal.thread.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditRegEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntrySmpEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * JournalThreadEntryEntity
 * <pre>
 *  저널 스레드-엔트리 소속 Entity.
 *
 *  스레드를 컨테이너로, 엔트리를 그 멤버로 잇는 N:M 조인 테이블이다.
 *  한 엔트리가 여러 스레드에 속할 수 있다 (FLOW 간선 모델이 표현하지 못하던 지점).
 *
 *  소속은 소프트 삭제({@code deleted_at})로만 해제한다. 행을 물리 삭제하지 않는다.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "journal_thread_entry")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE journal_thread_entry SET deleted_at = NOW() WHERE id = ?")
public class JournalThreadEntryEntity
        extends BaseAuditRegEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("journal thread entry id")
    private Integer id;

    @Column(name = "thread_id", nullable = false)
    @Comment("journal thread id")
    private Integer threadId;

    @Column(name = "entry_id", nullable = false)
    @Comment("journal entry id")
    private Integer entryId;

    /** 스레드 내 표시 순서. NULL 이면 엔트리 일자순으로 정렬한다. */
    @Column(name = "sort_order")
    @Comment("sort order in thread")
    private Integer sortOrder;

    /** 소속 스레드. 목록 조회 시 제목을 함께 내리기 위한 읽기 전용 조인. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", referencedColumnName = "id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("journal thread")
    private JournalThreadSmpEntity journalThread;

    /** 소속 엔트리. 스레드 상세에서 엔트리 정보를 함께 내리기 위한 읽기 전용 조인. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", referencedColumnName = "id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("journal entry")
    private JournalEntrySmpEntity journalEntry;
}
