package io.nicheblog.dreamdiary.feature.board.post.entity;

import io.nicheblog.dreamdiary.feature.board.def.entity.BoardDefEntity;
import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfEntity;
import io.nicheblog.dreamdiary.feature.clsf.comment.entity.embed.CommentEmbed;
import io.nicheblog.dreamdiary.feature.clsf.comment.entity.embed.CommentEmbedModule;
import io.nicheblog.dreamdiary.feature.clsf.file.entity.embed.AtchFileEmbed;
import io.nicheblog.dreamdiary.feature.clsf.file.entity.embed.AtchFileEmbedModule;
import io.nicheblog.dreamdiary.feature.clsf.managt.entity.embed.ManagtEmbed;
import io.nicheblog.dreamdiary.feature.clsf.managt.entity.embed.ManagtEmbedModule;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.embed.TagEmbedModule;
import io.nicheblog.dreamdiary.feature.clsf.viewer.entity.embed.ViewerEmbed;
import io.nicheblog.dreamdiary.feature.clsf.viewer.entity.embed.ViewerEmbedModule;
import io.nicheblog.dreamdiary.infrastructure.cd.Code;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * BoardPostEntity
 * <pre>
 *  게시판 게시물 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "board_post")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE board_post SET deleted_at = NOW() WHERE id = ?")
public class BoardPostEntity
        extends BaseClsfEntity
        implements AtchFileEmbedModule, CommentEmbedModule, TagEmbedModule, ManagtEmbedModule, ViewerEmbedModule {

    /** 글 번호 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("글 번호")
    private Integer id;

    /** 컨텐츠 타입 :: Override */
    @Column(name = "content_type")
    @Comment("컨텐츠 타입")
    private String contentType;

    /* ----- */

    /** 게시판 정의 정보 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_def", referencedColumnName = "board_def", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("게시판 정의 정보")
    private BoardDefEntity boardDefInfo;

    /** 제목 */
    @Column(name = "title")
    private String title;

    /** 내용 */
    @Column(name = "content")
    private String content;

    /* ----- */

    /** 중요 여부 (Y/N) */
    @Builder.Default
    @Column(name = "imprtc_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("중요 여부")
    private String imprtcYn = "N";

    /** 상단고정 여부 (Y/N) */
    @Builder.Default
    @Column(name = "fxd_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("상단고정 여부")
    private String fxdYn = "N";

    /** 조회수 */
    @Builder.Default
    @Column(name = "hit_cnt")
    private Integer hitCnt = 0;

    /** 수정권한 */
    @Builder.Default
    @Column(name = "mdfable")
    @Comment("수정권한")
    private String mdfable = Code.MDFABLE_REGSTR;

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
    /** 위임 :: 조치 정보 모듈 */
    @Embedded
    public ManagtEmbed managt;
    /** 위임 :: 열람 정보 모듈 */
    @Embedded
    public ViewerEmbed viewer;
}

