package io.nicheblog.dreamdiary.extension.tag.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * TagSearchParam
 * <pre>
 *  태그 목록 검색 파라미터.
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
public class TagSearchParam
        extends BaseSearchParam {

    /** 년도 */
    @Positive
    private Integer yy;

    /** 월 */
    @Positive
    private Integer mnth;

    /** 관리 컨텐츠 타입 */
    private String refContentType;

    /** 태그 카테고리 */
    private String ctgr;

    /** 컨텐츠 타입 */
    @Size(max = 50)
    private String contentType;
}
