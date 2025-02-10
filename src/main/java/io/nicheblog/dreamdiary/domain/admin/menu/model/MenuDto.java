package io.nicheblog.dreamdiary.domain.admin.menu.model;

import io.nicheblog.dreamdiary.extension.clsf.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.extension.clsf.state.model.cmpstn.StateCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
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

    /** 메뉴 번호 (PK)  */
    @Positive
    private Integer menuNo;

    /** 상위 메뉴 번호 */
    @Positive
    private Integer upperMenuNo;

    /** 메뉴 구분 코드 (루트"ROOT", 대메뉴"MAIN", 중-소메뉴"SUB") */
    @Size(max = 50)
    private String menuTyCd;

    /** 메뉴 구분 코드명 (루트"ROOT", 대메뉴"MAIN", 중-소메뉴"SUB") */
    @Size(max = 50)
    private String menuTyNm;

    /** 메뉴 이름 */
    @Size(max = 200)
    private String menuNm;

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
    private String menuSubExtendTyCd;

    /** 하위메뉴 확장유형 이름 */
    @Size(max = 50)
    private String menuSubExtendTyNm;

    /** 폴더(중메뉴) 여부 (Y/N) */
    @Builder.Default
    @Size(min = 1, max = 1)
    @Pattern(regexp = "^[YN]$")
    private String dirYn = "N";
    
    /** 관리자 메뉴 여부 (Y/N) */
    @Builder.Default
    private String mngrYn = "N";

    /** 셀프 참조 :: 상위메뉴 조회 */
    private MenuDto upperMenu;

    /** 셀프 참조 :: 상위메뉴명 */
    private String upperMenuNm;

    /** 셀프 참조 :: 상위메뉴구분코드 */
    private String upperMenuTyCd;

    /** 셀프 참조 :: 하위메뉴 목록 조회 */
    private List<MenuDto> subMenuList;

    /* ----- */

    /**
     * Getter :: 상위메뉴가 메인메뉴인지 여부 반환
     */
    public boolean getIsMain() {
        MenuDto upperMenu = this.upperMenu;
        if (upperMenu == null) return false;
        return "main".equals(upperMenu.getMenuTyCd());
    }

    /**
     * Getter :: 폴더(중메뉴) 여부 반환
     */
    public Boolean isDir() {
        return "Y".equals(this.dirYn);
    }

    /* ----- */

    @Override
    public Integer getKey() {
        return this.menuNo;
    }

    /** 위임 :: 상태 관리 모듈 */
    public StateCmpstn state;
}