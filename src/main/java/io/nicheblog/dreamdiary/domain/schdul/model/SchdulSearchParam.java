package io.nicheblog.dreamdiary.domain.schdul.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * SchdulSearchParam
 * <pre>
 *  일정 목록 검색 파라미터.
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
public class SchdulSearchParam
        extends BaseSearchParam {

    /** 조회시작일자 */
    private String bgnDt;

    /** 조회종료일자 */
    private String endDt;

    /** 스케쥴 코드 */
    private String schdulCd;

    /** 내 정보 표시 여부 (Y/N) */
    @Size(min = 1, max = 1)
    @Pattern(regexp = "^[YN]$")
    private String myPaprChked;

    /** 휴가 표시 여부 (Y/N) */
    @Size(min = 1, max = 1)
    @Pattern(regexp = "^[YN]$")
    private String vcatnChked;

    /** 내부일정 정보 표시 여부 (Y/N) */
    @Size(min = 1, max = 1)
    @Pattern(regexp = "^[YN]$")
    private String indtChked;

    /** 외근 정보 표시 여부 (Y/N) */
    @Size(min = 1, max = 1)
    @Pattern(regexp = "^[YN]$")
    private String outdtChked;

    /** 재택근무 정보 표시 여부 (Y/N) */
    @Size(min = 1, max = 1)
    @Pattern(regexp = "^[YN]$")
    private String tlcmmtChked;

    /** 개인용 정보 표시 여부 (Y/N) */
    @Size(min = 1, max = 1)
    @Pattern(regexp = "^[YN]$")
    private String prvtChked;

    /** 개인 일정만 조회할지 여부 */
    @Builder.Default
    private Boolean prevOnly = false;
}
