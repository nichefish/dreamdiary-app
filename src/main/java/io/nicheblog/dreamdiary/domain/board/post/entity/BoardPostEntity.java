package io.nicheblog.dreamdiary.domain.board.post.entity;

import io.nicheblog.dreamdiary.domain.board.def.entity.BoardDefEntity;
import io.nicheblog.dreamdiary.extension.comment.entity.embed.CommentEmbed;
import io.nicheblog.dreamdiary.extension.comment.entity.embed.CommentEmbedModule;
import io.nicheblog.dreamdiary.extension.managt.entity.embed.ManagtEmbed;
import io.nicheblog.dreamdiary.extension.managt.entity.embed.ManagtEmbedModule;
import io.nicheblog.dreamdiary.extension.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.extension.tag.entity.embed.TagEmbedModule;
import io.nicheblog.dreamdiary.extension.viewer.entity.embed.ViewerEmbed;
import io.nicheblog.dreamdiary.extension.viewer.entity.embed.ViewerEmbedModule;
import io.nicheblog.dreamdiary.global.intrfc.entity.BasePostEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;

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
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE board_post SET del_yn = 'Y' WHERE post_no = ?")
public class BoardPostEntity
        extends BasePostEntity
        implements CommentEmbedModule, TagEmbedModule, ManagtEmbedModule, ViewerEmbedModule {

    /** 글 번호 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_no")
    @Comment("글 번호")
    private Integer postNo;

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

    /* ----- */

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

