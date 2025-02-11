package io.nicheblog.dreamdiary.extension.cd.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * DtlCdSearchParam
 * <pre>
 *  상세 코드 목록 검색 파라미터.
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
public class DtlCdSearchParam
        extends BaseSearchParam {

    /** 분류 코드 */
    private String clCd;
}
