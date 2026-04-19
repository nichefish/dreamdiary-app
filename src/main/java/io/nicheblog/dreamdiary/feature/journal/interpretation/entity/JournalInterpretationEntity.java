package io.nicheblog.dreamdiary.feature.journal.interpretation.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.comment.entity.embed.CommentEmbed;
import io.nicheblog.dreamdiary.feature.attachable.comment.entity.embed.CommentEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.history.entity.embed.HistoryEmbed;
import io.nicheblog.dreamdiary.feature.attachable.history.entity.embed.HistoryEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.state.entity.embed.StateEmbed;
import io.nicheblog.dreamdiary.feature.attachable.state.entity.embed.StateEmbedModule;
import io.nicheblog.dreamdiary.feature.file.entity.embed.FileEmbed;
import io.nicheblog.dreamdiary.feature.file.entity.embed.FileEmbedModule;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDaySmpEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * JournalInterpretationEntity
 * <pre>
 *  저널 해석 Entity.
 *  Entity that contains each distinct interpretation.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "journal_interpretation")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE journal_interpretation SET deleted_at = NOW() WHERE id = ?")
public class JournalInterpretationEntity
        extends BaseAttachableEntity
        implements FileEmbedModule, CommentEmbedModule, StateEmbedModule, HistoryEmbedModule {

    /** 저널 해석 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("저널 해석 고유 번호")
    private Integer id;

    /** 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'JOURNAL_INTERPRETATION'")
    @Comment("컨텐츠 타입")
    private String contentType = ContentType.JOURNAL_INTERPRETATION.key;

    /** 제목 */
    @Column(name = "title")
    private String title;

    /** 내용 */
    @Column(name = "content")
    private String content;

    /* ----- */

    /** 참조 엔티티 번호 */
    @Column(name = "ref_id")
    @Comment("참조 엔티티 번호")
    private Integer refId;

    /** 참조 컨텐츠 타입 */
    @Enumerated(EnumType.STRING)
    @Column(name = "ref_content_type", length = 50)
    @Comment("참조 컨텐츠 타입")
    private ContentType refContentType;

    /** 저널 일자 번호 */
    @Column(name = "journal_day_id")
    @Comment("저널 일자 번호")
    private Integer journalDayId;

    /** 저널 일자 정보 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_day_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Comment("저널 일자 정보")
    private JournalDaySmpEntity journalDay;

    /** 순번 */
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 1")
    private Integer sortOrder;

    /**
     * 인덱스 변경 여부
     */
    @Builder.Default
    @Transient
    private Boolean isSortOrderChanged = false;

    /* ----- */

    /** 위임 :: 첨부파일 모듈 */
    @Embedded
    public FileEmbed file;
    /** 위임 :: 댓글 정보 모듈 */
    @Embedded
    public CommentEmbed comment;
    /** 위임 :: 상태 정보 모듈 */
    @Embedded
    public StateEmbed state;
    @Embedded
    public HistoryEmbed history;
}

