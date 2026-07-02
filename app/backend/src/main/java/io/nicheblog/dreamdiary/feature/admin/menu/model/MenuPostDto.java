package io.nicheblog.dreamdiary.feature.admin.menu.model;

import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotBlank;

/**
 * MenuPostDto
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
public class MenuPostDto
        extends BaseAuditDto
        implements Identifiable<Integer> {

    /** 메뉴 ID  */
    @Positive
    private Integer id;

    /** 상위 메뉴 번호 */
    @Positive
    private Integer parentMenuId;

    /** 메뉴 구분 코드 (루트"ROOT", 대메뉴"MAIN", 중-소메뉴"SUB") */
    @Size(max = 50)
    @Pattern(regexp = "^(MAIN|SUB)$")
    private String menuType;

    /** 메뉴 구분 코드명 (루트"ROOT", 대메뉴"MAIN", 중-소메뉴"SUB") */
    @Size(max = 50)
    private String menuTypeName;

    /** 메뉴 이름 */
    @Size(max = 200)
    private String menuName;

    /** 메뉴 라벨 */
    @Size(max = 100)
    @NotBlank
    private String menuLabel;

    /** 메뉴 breadcrumb 하단에 표시할 설명 */
    @Size(max = 1000)
    private String menuDescription;

    /** 미열람 카운트 이름 (model) */
    @Size(max = 100)
    private String unreadCntNm;

    /** URL */
    @Size(max = 1000)
    private String url;

    /** 관리자 메뉴 여부 (Y/N). 최상위 MAIN 메뉴에서만 직접 의미를 갖는다. */
    @Pattern(regexp = "^[YN]$")
    private String adminYn;

    /** 사용 여부 (Y/N) */
    @Pattern(regexp = "^[YN]$")
    private String useYn;

    /** 사이드바 표시 여부 (Y/N) */
    @Pattern(regexp = "^[YN]$")
    private String sidebarVisibleYn;

    /** 아이콘 (bootstrap icon 또는 font-awesome) TODO: svg? */
    private String icon;

    /** 하위메뉴 확장유형 코드 */
    @Size(max = 50)
    @Pattern(regexp = "^(NO_SUB|LIST|EXTEND|COLLAPSE|BOARD)$")
    private String submenuExpandType;

    /** 하위메뉴 확장유형 이름 */
    @Size(max = 50)
    private String submenuExpandTypeName;

    /* ----- */

    @Override
    public Integer getKey() {
        return this.id;
    }
}
