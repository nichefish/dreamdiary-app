package io.nicheblog.dreamdiary.domain.admin.menu.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * MenuSearchParam
 * <pre>
 *  메뉴 목록 검색 파라미터.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
public class MenuSearchParam
        extends BaseSearchParam {

    /** 메뉴 유형 코드 */
    @Size(max = 50)
    private String menuTyCd;

    /** 사용 여부 (Y/N) */
    @Size(min = 1, max = 1)
    @Pattern(regexp = "^[YN]$")
    private String useYn;

    /** 메뉴 이름 */
    @Size(max = 50)
    private String menuNm;

    /** 관리자 메뉴 여부 (Y/N) */
    @Size(min = 1, max = 1)
    @Pattern(regexp = "^[YN]$")
    private String mngrYn;
}
