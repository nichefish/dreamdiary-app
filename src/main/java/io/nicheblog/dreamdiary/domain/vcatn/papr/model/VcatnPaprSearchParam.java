package io.nicheblog.dreamdiary.domain.vcatn.papr.model;

import io.nicheblog.dreamdiary.extension.ContentType;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BasePostSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * VcatnPaprSearchParam
 * <pre>
 *  휴가계획서 목록 검색 파라미터.
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
public class VcatnPaprSearchParam
        extends BasePostSearchParam {

    /** 컨텐츠 타입 */
    @Builder.Default
    private String contentType = ContentType.VCATN_PAPR.key;
}
