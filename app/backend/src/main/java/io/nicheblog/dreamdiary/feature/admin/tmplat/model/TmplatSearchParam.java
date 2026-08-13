package io.nicheblog.dreamdiary.feature.admin.tmplat.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * TmplatSearchParam
 * <pre>
 *  템플릿 목록 검색 파라미터.
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
public class TmplatSearchParam extends BaseSearchParam {
    /** 사용 여부 (Y/N) 필터 */
    private String useYn;
}