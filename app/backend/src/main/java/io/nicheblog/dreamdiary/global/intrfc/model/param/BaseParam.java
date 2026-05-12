package io.nicheblog.dreamdiary.global.intrfc.model.param;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * BaseParam
 * <pre>
 *  (공통/상속) 파라미터.
 *  "All classes in the hierarchy must be annotated with @SuperBuilder."
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode
public class BaseParam {

    /** 액션구분 코드 */
    @Size(max = 50)
    protected String actionTyCd;

    /** 수정/상세에서 목록 화면으로 돌아옴 구분 코드 ( Y/N ) */
    @Builder.Default
    @Size(min = 1, max = 1)
    @Pattern(regexp = "^[YN]$")
    protected String isBackToList = "N";

    // TODO: UTM 파라미터 추가?

    /* ----- */

    /**
     * 목록으로 돌아가는지 여부를 확인하는 함수.
     *
     * @return {@link Boolean} -- "Y" 값과 일치하면 true, 그렇지 않으면 false.
     */
    public Boolean isBackToList() {
        return "Y".equals(this.isBackToList);
    }

    /**
     * 특정 액션 유형이 일치하는지 확인하는 함수.
     *
     * @param actionTyCd - 액션 유형 코드
     * @return {@link Boolean} -- 주어진 액션 유형 코드와 일치하면 true, 그렇지 않으면 false
     */
    public Boolean isAction(final String actionTyCd) {
        if (StringUtils.isEmpty(actionTyCd)) return false;
        return actionTyCd.equals(this.actionTyCd);
    }

    /* ----- */


}
