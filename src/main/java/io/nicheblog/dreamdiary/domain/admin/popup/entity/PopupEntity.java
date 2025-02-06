package io.nicheblog.dreamdiary.domain.admin.popup.entity;

import io.nicheblog.dreamdiary.extension.state.entity.embed.StateEmbed;
import io.nicheblog.dreamdiary.extension.state.entity.embed.StateEmbedModule;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseAtchEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

/**
 * PopupEntity
 * <pre>
 *  팝업 정보 관리 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "POPUP")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE popup SET del_yn = 'Y' WHERE popup_cd = ?")
public class PopupEntity
        extends BaseAtchEntity
        implements StateEmbedModule {

    /** 팝업 코드 */
    @Id
    @Column(name = "popup_cd")
    @Comment("팝업 코드")
    private String popupCd;

    /** 팝업 이름  */
    @Column(name = "popup_nm")
    @Comment("팝업 이름")
    private String popupNm;

    /** 가로 */
    @Column(name = "width")
    @Comment("가로")
    private Integer width;

    /** 세로 */
    @Column(name = "height")
    @Comment("세로")
    private Integer height;

    /** 게시시작일시 */
    @Column(name = "popup_start_dt")
    @Comment("게시시작일시")
    private Date popupStartDt;

    /** 게시종료일시 */
    @Column(name = "popup_end_dt")
    @Comment("게시종료일시")
    private Date popupEndDt;

    /* ----- */

    /** 위임 :: 상태 관리 모듈 */
    @Embedded
    public StateEmbed state;
}
