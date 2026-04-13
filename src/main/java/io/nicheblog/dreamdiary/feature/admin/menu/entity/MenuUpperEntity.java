package io.nicheblog.dreamdiary.feature.admin.menu.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
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
 * MenuUpperEntity
 * <pre>
 *  상위 메뉴용 매뉴 관리 간소화 Entity.
 *  (순환참조 방지 위해 상위메뉴 entity에 대해서 하위메뉴 목록 참조 삭제)
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
@SQLDelete(sql = "UPDATE menu SET del_yn = 'Y' WHERE id = ?")
public class MenuUpperEntity
        extends BaseAuditEntity {

    /** 메뉴 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("메뉴 ID")
    private Integer id;

    /** 상위메뉴 ID */
    @Column(name = "upper_menu_id")
    @Comment("상위 메뉴 번호")
    private Integer upperMenuId;

    /** 메뉴 구분 코드 */
    @Column(name = "menu_ty_cd")
    @Comment("메뉴 구분 코드")
    private String menuTyCd;

    /** 메뉴명 */
    @Column(name = "menu_nm")
    @Comment("메뉴명")
    private String menuNm;

    /** URL  */
    @Column(name = "url")
    @Comment("URL")
    private String url;

    /** 아이콘 (bootstrap icon 또는 font-awesome) TODO: svg? */
    @Column(name = "icon")
    @Comment("아이콘")
    private String icon;

    /** 셀프 참조 :: 상위메뉴 조회 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "upper_menu_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(value = FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("상위메뉴 조회")
    private MenuUpperEntity upperMenu;
}
