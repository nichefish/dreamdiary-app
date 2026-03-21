package io.nicheblog.dreamdiary.feature.clsf.state.model;

import lombok.*;

import javax.validation.constraints.Positive;

/**
 * CacheContext
 *
 * @author nichefish
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheContext {

    /** 글 번호 */
    @Positive
    private Integer yy;
    /** 컨텐츠 타입 */
    @Positive
    private Integer mnth;
}
