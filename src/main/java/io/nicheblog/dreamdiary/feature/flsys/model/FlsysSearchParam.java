package io.nicheblog.dreamdiary.feature.flsys.model;

import io.nicheblog.dreamdiary.feature.clsf._shared.model.param.BaseClsfSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * FlsysSearchParam
 * <pre>
 *  파일시스템 목록 검색 파라미터.
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
public class FlsysSearchParam
        extends BaseClsfSearchParam {

    /** 참조 글 번호 */
    @Positive
    private Integer refId;

    /** 참조 컨텐츠 타입 */
    @Size(max = 50)
    private String refContentType;

}
