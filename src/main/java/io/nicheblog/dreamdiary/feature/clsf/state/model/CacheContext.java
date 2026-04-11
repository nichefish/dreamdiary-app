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

    /** 연도 */
    @Positive
    private Integer yy;
    /** 월 */
    @Positive
    private Integer mnth;
    /** 주 시작일 */
    private String weekStartDt;
}
