package io.nicheblog.dreamdiary.extension.cd.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * DtlCdParam
 * <pre>
 *  상세 코드 파라미터.
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
public class DtlCdParam
        extends BaseParam {

    /** 정렬순서 배열 */
    private List<DtlCdDto> sortOrdr;
}
