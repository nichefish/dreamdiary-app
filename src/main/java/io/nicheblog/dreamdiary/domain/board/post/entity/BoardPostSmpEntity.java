package io.nicheblog.dreamdiary.domain.board.post.entity;

import io.nicheblog.dreamdiary.domain.board.def.entity.BoardDefEntity;
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
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE board_post SET del_yn = 'Y' WHERE post_no = ?")
public class BoardPostSmpEntity
        extends BasePostEntity {

    /** 글 번호 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_no")
    @Comment("글 번호")
    private Integer postNo;

    /** 컨텐츠 타입 :: Override */
    @Column(name = "content_type")
    private String contentType;

    /* ----- */

    /** 게시판 정의 정보 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_def", referencedColumnName = "board_def", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("게시판 정의 정보")
    private BoardDefEntity boardDefInfo;
}

