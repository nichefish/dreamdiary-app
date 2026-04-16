package io.nicheblog.dreamdiary.feature.board.post.entity;

import io.nicheblog.dreamdiary.feature.board.def.entity.BoardDefEntity;
import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import lombok.*;
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
public class BoardPostSmpEntity
        extends BaseAttachableEntity {

    /** 글 번호 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("글 번호")
    private Integer id;

    /** 컨텐츠 타입 :: Override */
    @Column(name = "content_type")
    private String contentType;

    /* ----- */

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

    /** 게시판 정의 정보 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_def", referencedColumnName = "board_def", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("게시판 정의 정보")
    private BoardDefEntity boardDefInfo;
}

