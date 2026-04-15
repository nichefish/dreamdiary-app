package io.nicheblog.dreamdiary.feature.clsf.history.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditRegEntity;
import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.feature.clsf.history.HistoryType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * HistoryEntity
 * <pre>
 *  이력 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "history")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE history SET deleted_at = NOW() WHERE id = ?")
public class HistoryEntity
        extends BaseAuditRegEntity {

    /** 이력 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("이력 ID")
    private Integer id;

    /** 참조 글번호 */
    @Column(name = "ref_id")
    @Comment("참조 글번호")
    private Integer refId;

    /** 참조 컨텐츠 타입 */
    @Column(name = "ref_content_type")
    @Comment("참조 컨텐츠 타입")
    private String refContentType;

    @Lob
    @Column(name = "content")
    @Comment("이력 내용 스냅샷")
    private String content;

    @Builder.Default
    @Column(name = "history_type", length = 20, nullable = false)
    @Comment("이력 타입")
    private String historyType = HistoryType.CHANGE.key;

    @Column(name = "from_history_id")
    @Comment("복구 원본 이력 번호")
    private Integer fromHistoryId;

    /* ----- */

    /**
     * 생성자.
     *
     * @param refKey 글 번호와 컨텐츠 타입을 포함하는 참조 복합키 객체
     */
    public HistoryEntity(final BaseClsfKey refKey) {
        this.refId = refKey.getId();
        this.refContentType = refKey.getContentType();
    }

    /**
     * 생성자.
     *
     * @param refKey 글 번호와 컨텐츠 타입을 포함하는 참조 복합키 객체
     * @param content 변경내용
     */
    public HistoryEntity(final BaseClsfKey refKey, final String content) {
        this(refKey);
        this.content = content;
    }

    /**
     * 생성자.
     *
     * @param refKey 글 번호와 컨텐츠 타입을 포함하는 참조 복합키 객체
     * @param content 변경내용
     * @param historyType 이력 타입
     * @param fromHistoryId 이력 복구 번호
     */
    public HistoryEntity(final BaseClsfKey refKey, final String content, final HistoryType historyType, final Integer fromHistoryId) {
        this(refKey, content);
        this.historyType = historyType != null ? historyType.key : HistoryType.CHANGE.key;
        this.fromHistoryId = fromHistoryId;
    }
}
