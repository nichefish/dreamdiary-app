package io.nicheblog.dreamdiary.feature.board.post.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.embed.PrefixEmbed;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.embed.PrefixEmbedModule;
import io.nicheblog.dreamdiary.feature.board.group.entity.BoardEntity;
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
 * BoardPostSmpEntity
 * <pre>
 *  게시판 게시물 간소화 Entity.
 *  (BoardPostEntity에서 연관관계(댓글, 태그 등) 정보 제거 = 연관관계 순환참조 방지 위함. 나머지는 동일)
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
public class BoardPostSmpEntity extends BaseAttachableEntity implements PrefixEmbedModule {

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

    /** 제목 */
    @Column(name = "title", length = 200)
    @Comment("title")
    private String title;

    /** 내용 */
    @Column(name = "content")
    @Comment("content")
    private String content;

    /**
     * 게시판 GLOBAL Scope 소속 단일 말머리.
     * 전체 엔티티와 동일하게 prefix_content 연결을 PrefixEmbed로 조립한다.
     */
    @Embedded
    public PrefixEmbed prefix;

    /* ----- */

    /** 게시판 정의 정보 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_type", referencedColumnName = "board_key", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("board account")
    private BoardEntity boardInfo;
}
