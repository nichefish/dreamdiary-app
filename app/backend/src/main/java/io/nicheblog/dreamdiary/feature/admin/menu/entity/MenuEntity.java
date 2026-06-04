package io.nicheblog.dreamdiary.feature.admin.menu.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.Sortable;
import io.nicheblog.dreamdiary.global.intrfc.entity.Usable;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
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
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE menu SET deleted_at = NOW() WHERE id = ?")
public class MenuEntity
        extends BaseAuditEntity
        implements Usable, Sortable {

    /** 메뉴 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("메뉴 ID")
    private Integer id;

    /** 상위메뉴 ID */
    @Column(name = "parent_menu_id")
    @Comment("상위 메뉴 번호")
    private Integer parentMenuId;

    /** 메뉴 구분 코드 */
    @Column(name = "menu_type")
    @Comment("메뉴 구분 코드")
    private String menuType;

    /** 관리자 메뉴 여부 (Y/N) */
    @Builder.Default
    @Column(name = "admin_yn")
    @Comment("관리자 메뉴 여부 (Y/N)")
    private String adminYn = "N";

    /** 메뉴명 */
    @Column(name = "menu_name")
    @Comment("메뉴명")
    private String menuName;

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

    /** 시스템 보호 여부 (Y/N) */
    @Builder.Default
    @Column(name = "protected_yn")
    @Comment("시스템 보호 여부 (Y/N)")
    private String protectedYn = "N";

    /** 하위메뉴 확장유형 */
    @Column(name = "submenu_expand_type")
    @Comment("하위메뉴 확장유형")
    private String submenuExpandType;

    /** 하위메뉴 확장유형 이름 */
    @Transient
    private String submenuExpandTypeName;

    /** 정렬 순서 */
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder;

    /** 사용 여부 (Y/N) */
    @Builder.Default
    @Column(name = "use_yn", length = 1, columnDefinition = "CHAR DEFAULT 'Y'")
    private String useYn = "N";

    /** 셀프 참조 :: 상위메뉴 조회 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_menu_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(value = FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("상위메뉴 조회")
    private MenuUpperEntity upperMenu;

    /** 셀프 참조 :: 하위메뉴 목록 조회 */
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_menu_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 10)
    @OrderBy("sortOrder ASC")
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("하위메뉴 목록 조회")
    private List<MenuEntity> subMenuList;
}
