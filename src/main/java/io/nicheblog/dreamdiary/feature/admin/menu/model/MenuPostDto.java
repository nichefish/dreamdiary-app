package io.nicheblog.dreamdiary.feature.admin.menu.model;

import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

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

    /* ----- */

    @Override
    public Integer getKey() {
        return this.menuNo;
    }
}
