package io.nicheblog.dreamdiary.domain.admin.menu.entity;

import io.nicheblog.dreamdiary.extension.state.entity.embed.StateEmbed;
import io.nicheblog.dreamdiary.extension.state.entity.embed.StateEmbedModule;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseAuditEntity;
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
@SQLDelete(sql = "UPDATE menu SET del_yn = 'Y' WHERE menu_no = ?")
public class MenuUpperEntity
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
    @JoinColumn(name = "upper_menu_no", referencedColumnName = "menu_no", insertable = false, updatable = false)
    @Fetch(value = FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("상위메뉴 조회")
    private MenuUpperEntity upperMenu;

    /* ----- */

    /** 위임 :: 상태 관리 모듈 */
    @Embedded
    public StateEmbed state;
}
