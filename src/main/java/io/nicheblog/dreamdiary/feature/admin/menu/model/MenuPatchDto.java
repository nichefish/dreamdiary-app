package io.nicheblog.dreamdiary.feature.admin.menu.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.tika.utils.StringUtils;

/**
 * MenuPatchDto
 * <pre>
 *  메뉴 상태 변경 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class MenuPatchDto {

    /** 중요 여부 */
    private String useYn;

    /**
     * 요청이 전부 공백인 경우 판별
     * @return 전부 공백 여부
     */
    public boolean isAllNull() {
        return StringUtils.isEmpty(useYn);
    }
}
