package io.nicheblog.dreamdiary.feature.calendar.schedule.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * ScheduleSearchParam
 * <pre>
 *  일정 목록 검색 파라미터.
 *  변경 전에는 내부 전용 prevOnly를 getPrvtOnly 조건으로 변환하지 못해 개인 일정 조회가
 *  사실상 무제한 조회로 새었다. 변경 후에는 prvtChked 하나가 공개/참가 개인 일정 가시성을 결정한다.
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
public class ScheduleSearchParam
        extends BaseSearchParam {

    /** 조회시작일자 */
    private String bgnDt;

    /** 조회종료일자 */
    private String endDt;

    /** 스케쥴 코드 */
    private String scheduleCd;

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

}
