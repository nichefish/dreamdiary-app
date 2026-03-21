package io.nicheblog.dreamdiary.feature.jrnl.intrpt.entity;

import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.comment.entity.embed.CommentEmbed;
import io.nicheblog.dreamdiary.feature.clsf.comment.entity.embed.CommentEmbedModule;
import io.nicheblog.dreamdiary.feature.clsf.state.entity.embed.StateEmbed;
import io.nicheblog.dreamdiary.feature.clsf.state.entity.embed.StateEmbedModule;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.embed.TagEmbedModule;
import io.nicheblog.dreamdiary.feature.jrnl.dream.entity.JrnlDreamEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.embed.AtchFileEmbed;
import io.nicheblog.dreamdiary.global.intrfc.entity.embed.AtchFileEmbedModule;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * JrnlIntrptEntity
 * <pre>
 *  저널 해석 Entity.
 *  Entity that contains each distinct intrpt.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "jrnl_intrpt")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE jrnl_intrpt SET del_yn = 'Y' WHERE post_no = ?")
public class JrnlIntrptEntity
        extends BaseClsfEntity
        implements AtchFileEmbedModule, CommentEmbedModule, TagEmbedModule, StateEmbedModule {

    /** 저널 해석 고유 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_no")
    @Comment("저널 해석 고유 번호")
    private Integer postNo;

    /** 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'JRNL_ENTRY'")
    @Comment("컨텐츠 타입")
    private String contentType = ContentType.JRNL_INTRPT.key;

    /** 제목 */
    @Column(name = "title")
    protected String title;

    /** 내용 */
    @Column(name = "cn")
    protected String cn;

    /* ----- */

    /** 저널 꿈 정보 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jrnl_dream_no", nullable = false)
    @Comment("저널 꿈 정보")
    private JrnlDreamEntity jrnlDream;

    /** 순번 */
    @Column(name = "idx", columnDefinition = "INT DEFAULT 1")
    private Integer idx;

    /**
     * 인덱스 변경 여부
     */
    @Builder.Default
    @Transient
    private Boolean isIdxChanged = false;

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
    /** 위임 :: 상태 정보 모듈 */
    @Embedded
    public StateEmbed state;
}
