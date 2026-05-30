package io.nicheblog.dreamdiary.feature.admin.menu.model;

import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SubmenuExpandType;
import io.nicheblog.dreamdiary.feature.attachable.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.state.model.cmpstn.StateCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.validator.state.UpdateState;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * MenuDto
 * <pre>
 *  메뉴 목록 조회 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class MenuDto
        extends BaseAuditDto
        implements Identifiable<Integer>, StateCmpstnModule {

    /** 메뉴 ID  */
    @Positive
    private Integer id;

    /** 상위 메뉴 번호 */
    @Positive
    private Integer parentMenuId;

    /** 메뉴 구분 코드 (루트"ROOT", 대메뉴"MAIN", 중-소메뉴"SUB") */
    @Size(max = 50)
    private String menuType;

    /** 메뉴 구분 코드명 (루트"ROOT", 대메뉴"MAIN", 중-소메뉴"SUB") */
    @Size(max = 50)
    private String menuTypeName;

    /** 메뉴 이름 */
    @Size(max = 200)
    private String menuName;

    /** 메뉴 라벨 */
    @Size(max = 100)
    private String menuLabel;

    /** 미열람 카운트 이름 (model) */
    @Size(max = 100)
    private String unreadCntNm;

    /** URL */
    @Size(max = 1000)
    private String url;

    /** 아이콘 (bootstrap icon 또는 font-awesome) TODO: svg? */
    private String icon;

    /** 하위메뉴 확장유형 코드 */
    @Size(max = 50)
    private String submenuExpandType;

    /** 하위메뉴 확장유형 이름 */
    @Size(max = 50)
    private String submenuExpandTypeName;

    /** 정렬 순서 */
    private Integer sortOrder;

    /** 사용 여부 (Y/N) */
    @Builder.Default
    private String useYn = "N";

    /** 폴더(중메뉴) 여부 (Y/N) */
    @Builder.Default
    @Pattern(regexp = "^[YN]$", groups = UpdateState.class)
    private String dirYn = "N";
    
    /** 관리자 메뉴 여부 (Y/N) */
    @Builder.Default
    private String adminYn = "N";

    /** 필수 여부 (Y/N) */
    @Builder.Default
    private String requiredYn = "N";
    /** 시스템 보호 여부 (Y/N) */
    @Builder.Default
    private String protectedYn = "N";

    /** 셀프 참조 :: 상위메뉴 조회 */
    private MenuDto upperMenu;
    /** 셀프 참조 :: 상위메뉴명 */
    private String upperMenuNm;
    /** 셀프 참조 :: 상위메뉴구분코드 */
    private String parentMenuType;
    /** 셀프 참조 :: 하위메뉴 목록 조회 */
    private List<MenuDto> subMenuList;

    /* ----- */

    /**
     * Getter :: 상위메뉴가 메인메뉴인지 여부 반환
     */
    public boolean getIsMain() {
        MenuDto upperMenu = this.upperMenu;
        if (upperMenu == null) return false;
        return "main".equals(upperMenu.getMenuType());
    }

    /**
     * Getter :: 폴더(중메뉴) 여부 반환
     */
    public Boolean isDir() {
        return "Y".equals(this.dirYn);
    }

    public String getSubmenuExpandTypeName() {
        if (this.submenuExpandTypeName != null && !this.submenuExpandTypeName.isBlank()) {
            return this.submenuExpandTypeName;
        }
        return SubmenuExpandType.getDesc(this.submenuExpandType);
    }

    /* ----- */

    @Override
    public Integer getKey() {
        return this.id;
    }

    /** 위임 :: 상태 관리 모듈 */
    public StateCmpstn state;
}
