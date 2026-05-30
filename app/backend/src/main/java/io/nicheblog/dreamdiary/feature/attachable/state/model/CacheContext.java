package io.nicheblog.dreamdiary.feature.attachable.state.model;

import lombok.*;

import javax.validation.constraints.Positive;

/**
 * CacheContext
 * 상태 캐시 조회·무효화 시 연·월·주 시작일 등을 묶어 전달하는 맥락 DTO.
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
