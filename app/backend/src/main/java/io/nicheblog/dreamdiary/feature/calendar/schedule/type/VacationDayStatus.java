package io.nicheblog.dreamdiary.feature.calendar.schedule.type;

import java.util.Set;

/**
 * 저널 일자에 투영하는 사용자 휴가의 시간 범위 상태.
 * 공휴일·주말 상태와는 별도 축이며, 정책에 등록되지 않은 휴가 코드를 전일로 추정하지 않는다.
 */
public enum VacationDayStatus {
    NONE,
    FULL_DAY,
    AM_HALF,
    PM_HALF,
    UNKNOWN;

    private static final String AM_HALF_CODE = "AM_HALF";
    private static final String PM_HALF_CODE = "PM_HALF";
    private static final Set<String> FULL_DAY_CODES = Set.of(
            "ANNUAL",
            "PBLEN",
            "CTSNN",
            "MNSTR",
            "UNPAID"
    );

    /**
     * 휴가 세부 코드를 일자 시간 범위 상태로 판정한다.
     * 신규 코드가 추가되면 코드의 시간 의미를 확인한 뒤 이 정책에도 명시해야 한다.
     *
     * @param vcatnCd 휴가 세부 코드
     * @return 휴가 일자 상태
     */
    public static VacationDayStatus fromVacationCode(final String vcatnCd) {
        if (vcatnCd == null) return UNKNOWN;
        if (AM_HALF_CODE.equals(vcatnCd)) return AM_HALF;
        if (PM_HALF_CODE.equals(vcatnCd)) return PM_HALF;
        if (FULL_DAY_CODES.contains(vcatnCd)) return FULL_DAY;
        return UNKNOWN;
    }

    /**
     * 같은 날짜에 겹친 휴가 상태를 보수적으로 병합한다.
     * 오전·오후 반차 조합은 전일이고, UNKNOWN은 전일이 확정된 경우 외에는 유지한다.
     *
     * @param other 추가 상태
     * @return 병합 상태
     */
    public VacationDayStatus merge(final VacationDayStatus other) {
        if (other == null || other == NONE) return this;
        if (this == NONE) return other;
        if (this == FULL_DAY || other == FULL_DAY) return FULL_DAY;
        if (this == UNKNOWN || other == UNKNOWN) return UNKNOWN;
        if (this != other) return FULL_DAY;
        return this;
    }
}
