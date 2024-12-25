package io.nicheblog.dreamdiary.domain.board.post.entity;

import io.nicheblog.dreamdiary.domain.board.def.entity.BoardDefEntity;
import io.nicheblog.dreamdiary.global._common._clsf.comment.entity.embed.CommentEmbed;
import io.nicheblog.dreamdiary.global._common._clsf.comment.entity.embed.CommentEmbedModule;
import io.nicheblog.dreamdiary.global._common._clsf.managt.entity.embed.ManagtEmbed;
import io.nicheblog.dreamdiary.global._common._clsf.managt.entity.embed.ManagtEmbedModule;
import io.nicheblog.dreamdiary.global._common._clsf.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.global._common._clsf.tag.entity.embed.TagEmbedModule;
import io.nicheblog.dreamdiary.global._common._clsf.viewer.entity.embed.ViewerEmbed;
import io.nicheblog.dreamdiary.global._common._clsf.viewer.entity.embed.ViewerEmbedModule;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
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
@IdClass(BaseClsfKey.class)      // 분류 코드+상세 코드 복합키 적용
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE board_post SET del_yn = 'Y' WHERE content_type = ? AND post_no = ?")
public class BoardPostEntity
        extends BasePostEntity
        implements CommentEmbedModule, TagEmbedModule, ManagtEmbedModule, ViewerEmbedModule {

    /** 글 번호 :: 복합키 사용, 시퀀스 생성 로직을 위해 재정의 */
    @Id
    @TableGenerator(name = "board_post", table = "cmm_sequence", pkColumnName = "seq_nm", valueColumnName = "seq_val", pkColumnValue = "post_no", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "board_post")
    @Column(name = "post_no")
    @Comment("글번호 (key)")
    private Integer postNo;

    /** 컨텐츠 타입 :: Override */
    @Id
    @Column(name = "content_type")
    @Comment("컨텐츠 타입")
    private String contentType;

    /* ----- */

    /** 게시판 정의 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "content_type", referencedColumnName = "board_cd", insertable = false, updatable = false)
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

