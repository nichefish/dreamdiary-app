package io.nicheblog.dreamdiary.feature.board.post.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.attachable.comment.entity.embed.CommentEmbed;
import io.nicheblog.dreamdiary.feature.attachable.comment.entity.embed.CommentEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.embed.TagEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.viewer.entity.embed.ViewerEmbed;
import io.nicheblog.dreamdiary.feature.attachable.viewer.entity.embed.ViewerEmbedModule;
import io.nicheblog.dreamdiary.feature.board.group.entity.BoardEntity;
import io.nicheblog.dreamdiary.feature.file.entity.embed.FileEmbed;
import io.nicheblog.dreamdiary.feature.file.entity.embed.FileEmbedModule;
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
        extends BaseAttachableEntity
        implements FileEmbedModule, CommentEmbedModule, TagEmbedModule, ViewerEmbedModule {

    /** 글 번호 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("post id")
    private Integer id;

    /** 컨텐츠 타입 :: Override */
    @Column(name = "content_type", length = 30)
    @Comment("board key via content type")
    private String contentType;

    /* ----- */

    /** 게시판 정의 정보 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_type", referencedColumnName = "board_key", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("board account")
    private BoardEntity boardInfo;

    /** 제목 */
    @Column(name = "title", length = 200)
    @Comment("title")
    private String title;

    /** 내용 */
    @Column(name = "content")
    @Comment("content")
    private String content;

    /** 글 분류 코드 */
    @Column(name = "category_code", length = 50)
    @Comment("category code")
    private String categoryCode;

    /* ----- */

    /** 위임 :: 첨부파일 모듈 */
    @Embedded
    public FileEmbed file;
    /** 위임 :: 댓글 정보 모듈 */
    @Embedded
    public CommentEmbed comment;
    /** 위임 :: 태그 정보 모듈 */
    @Embedded
    public TagEmbed tag;
    /** 위임 :: 열람 정보 모듈 */
    @Embedded
    public ViewerEmbed viewer;
}
