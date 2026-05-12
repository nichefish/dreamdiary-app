package io.nicheblog.dreamdiary.feature.admin.tmplat.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * TmplatDefSearchParam
 * <pre>
 *  템플릿 정의 목록 검색 파라미터.
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
public class TmplatDefSearchParam
        extends BaseSearchParam {

    /** 제목 */
    private String title;
}
