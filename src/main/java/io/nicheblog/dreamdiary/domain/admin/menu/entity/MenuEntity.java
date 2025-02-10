package io.nicheblog.dreamdiary.domain.admin.menu.entity;

import io.nicheblog.dreamdiary.extension.clsf.state.entity.embed.StateEmbed;
import io.nicheblog.dreamdiary.extension.clsf.state.entity.embed.StateEmbedModule;
import io.nicheblog.dreamdiary.extension.cd.entity.DtlCdEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseAuditEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.List;

/**
 * MenuEntity
 * <pre>
 *  메뉴 관리 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "menu")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE menu SET del_yn = 'Y' WHERE menu_no = ?")
public class MenuEntity
        extends BaseAuditEntity
        implements StateEmbedModule {

    /** 메뉴 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_no")
    @Comment("메뉴 번호 (PK)")
    private Integer menuNo;

    /** 상위메뉴 ID */
    @Column(name = "upper_menu_no")
    @Comment("상위 메뉴 번호")
    private Integer upperMenuNo;

    /** 메뉴 구분 코드 */
    @Column(name = "menu_ty_cd")
    @Comment("메뉴 구분 코드")
    private String menuTyCd;

    /** 관리자 메뉴 여부 (Y/N) */
    @Builder.Default
    @Column(name = "mngr_yn")
    @Comment("관리자 메뉴 여부 (Y/N)")
    private String mngrYn = "N";

    /** 메뉴명 */
    @Column(name = "menu_nm")
    @Comment("메뉴명")
    private String menuNm;

    /** 메뉴 라벨 */
    @Column(name = "menu_label")
    @Comment("메뉴 라벨")
    private String menuLabel;

    /** 미열람 카운트 이름 (model) */
    @Column(name = "unread_cnt_nm")
    @Comment("미열람 카운트 이름 (model)")
    private String unreadCntNm;

    /** URL  */
    @Column(name = "url")
    @Comment("URL")
    private String url;

    /** 아이콘 (bootstrap icon 또는 font-awesome) TODO: svg? */
    @Column(name = "icon")
    @Comment("아이콘")
    private String icon;

    /** 하위메뉴 확장유형 코드 */
    @Column(name = "menu_sub_extend_ty_cd")
    @Comment("하위메뉴 확장유형 코드")
    private String menuSubExtendTyCd;

    /** 메뉴 구분 코드 정보 (복합키 조인) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(formula = @JoinFormula(value = "\'MENU_SUB_EXTEND_TY_CD\'", referencedColumnName = "cl_cd")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "menu_sub_extend_ty_cd", referencedColumnName = "dtl_cd", insertable = false, updatable = false))
    })
    @Fetch(value = FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("메뉴 구분 코드 정보")
    private DtlCdEntity menuSubExtendTyCdInfo;

    /** 하위메뉴 확장유형 이름 */
    @Transient
    private String menuSubExtendTyNm;

    /** 셀프 참조 :: 상위메뉴 조회 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "upper_menu_no", referencedColumnName = "menu_no", insertable = false, updatable = false)
    @Fetch(value = FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("상위메뉴 조회")
    private MenuUpperEntity upperMenu;

    /** 셀프 참조 :: 하위메뉴 목록 조회 */
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "upper_menu_no", referencedColumnName = "menu_no", insertable = false, updatable = false)
    @Fetch(FetchMode.SELECT)
    @OrderBy("state.sortOrdr ASC")
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("하위메뉴 목록 조회")
    private List<MenuEntity> subMenuList;

    /* ----- */

    /** 위임 :: 상태 관리 모듈 */
    @Embedded
    public StateEmbed state;
}
