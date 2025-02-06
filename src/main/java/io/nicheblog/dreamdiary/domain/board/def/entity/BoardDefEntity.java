package io.nicheblog.dreamdiary.domain.board.def.entity;

import io.nicheblog.dreamdiary.extension.state.entity.embed.StateEmbed;
import io.nicheblog.dreamdiary.extension.state.entity.embed.StateEmbedModule;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseAuditEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * BoardDefEntity
 * <pre>
 *  게시판 정의 정보 Entity.
 *  ※게시판 정의(board_def) = 게시판 분류. 게시판 게시물(board_post)을 1:N으로 관리한다.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "board_def")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
public class BoardDefEntity
        extends BaseAuditEntity
        implements StateEmbedModule {

    /** 게시판 정의 (PK) */
    @Id
    @Column(name = "board_def")
    @Comment("게시판 정의 (PK)")
    private String boardDef;

    /** 게시판 이름 */
    @Column(name = "board_nm")
    @Comment("게시판 이름")
    private String boardNm;

    /** 게시판 글분류 코드 */
    @Column(name = "ctgr_cl_cd")
    @Comment("게시판 글분류 코드")
    private String ctgrClCd;

    /** 설명 */
    @Column(name = "dc", length=2000)
    private String dc;

    /* ----- */

    /** 위임 :: 상태 관리 모듈 */
    @Embedded
    public StateEmbed state;
}