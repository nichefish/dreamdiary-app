package io.nicheblog.dreamdiary.feature.journal.sumry.entity;

import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfEntity;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.comment.entity.embed.CommentEmbed;
import io.nicheblog.dreamdiary.feature.clsf.comment.entity.embed.CommentEmbedModule;
import io.nicheblog.dreamdiary.feature.clsf.file.entity.embed.AtchFileEmbed;
import io.nicheblog.dreamdiary.feature.clsf.file.entity.embed.AtchFileEmbedModule;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.embed.TagEmbedModule;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * JournalSumryReviewEntity
 * <pre>
 *  저널 결산 리뷰 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "journal_sumry_review")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE journal_sumry_review SET deleted_at = NOW() WHERE id = ?")
public class JournalSumryReviewEntity
        extends BaseClsfEntity
        implements CommentEmbedModule, TagEmbedModule, AtchFileEmbedModule {

    /** 저널 꿈 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("저널 결산 리뷰 고유 번호")
    private Integer id;

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'JOURNAL_SUMRY_REVIEW'")

    @Comment("컨텐츠 타입")
    private String contentType = ContentType.JOURNAL_SUMRY_REVIEW.key;

    /** 제목 */
    @Column(name = "title")
    private String title;

    /** 내용 */
    @Column(name = "cn")
    private String cn;

    /* ----- */

    /** 저널 결산 번호  */
    @Column(name = "journal_sumry_id")
    @Comment("저널 결산 번호")
    private Integer journalSumryId;

    /** 저널 결산 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "journal_sumry_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("저널 일자 정보")
    private JournalSumryEntity journalSumry;

    /** 순번 */
    @Column(name = "idx", columnDefinition = "INT DEFAULT 1")
    private Integer idx;

    /* ----- */

    /** 위임 :: 첨부파일 모듈 */
    @Embedded
    public AtchFileEmbed file;
    /** 위임 :: 댓글 정보 모듈 */
    @Embedded
    public CommentEmbed comment;
    /** 위임 :: 태그 정보 모듈 */
    @Embedded
    public TagEmbed tag;
}

