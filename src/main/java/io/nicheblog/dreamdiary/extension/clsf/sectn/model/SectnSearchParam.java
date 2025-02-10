package io.nicheblog.dreamdiary.extension.clsf.sectn.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * SectnSearchParam
 * <pre>
 *  단락 목록 검색 파라미터.
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
public class SectnSearchParam
        extends BaseSearchParam {

    /** 참조 글 번호 */
    @Positive
    private Integer refPostNo;

    /** 참조 컨텐츠 타입 */
    @Size(max = 50)
    private String refContentType;
}
