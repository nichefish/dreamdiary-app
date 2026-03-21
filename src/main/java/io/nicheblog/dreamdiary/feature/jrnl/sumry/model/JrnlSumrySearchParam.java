package io.nicheblog.dreamdiary.feature.jrnl.sumry.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * JrnlSumrySearchParam
 * <pre>
 *  저널 결산 목록 검색 파라미터.
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
public class JrnlSumrySearchParam
        extends BaseSearchParam {

    /** 년도 */
    private Integer yy;

    /** 월 */
    private Integer mnth;
}
