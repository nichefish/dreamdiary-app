package io.nicheblog.dreamdiary.feature.journal.day.service.helper;

import io.nicheblog.dreamdiary.feature.calendar.schedule.model.VacationDayInfo;
import io.nicheblog.dreamdiary.feature.calendar.schedule.type.VacationDayStatus;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;

/**
 * 저널 일자 DTO에 현재 사용자 참가 휴가 정보를 적용한다.
 */
@UtilityClass
public final class JournalDayVacationHelper {

    /**
     * 캐시된 저널 DTO에 이전 휴가 투영값이 남지 않도록 모든 일자를 새 조회 결과로 덮어쓴다.
     *
     * @param journalDayList 저널 일자 목록
     * @param vacationDayMap 날짜별 휴가 정보
     */
    public static void setVacationInfo(
            final List<JournalDayDto> journalDayList,
            final Map<String, VacationDayInfo> vacationDayMap
    ) {
        if (journalDayList == null || journalDayList.isEmpty()) return;
        for (final JournalDayDto journalDay : journalDayList) {
            setVacationInfo(journalDay, vacationDayMap);
        }
    }

    /**
     * 저널 일자 한 건에 휴가 정보를 적용한다. 조회 결과가 없으면 NONE·빈 사유로 초기화한다.
     *
     * @param journalDay 저널 일자
     * @param vacationDayMap 날짜별 휴가 정보
     */
    public static void setVacationInfo(
            final JournalDayDto journalDay,
            final Map<String, VacationDayInfo> vacationDayMap
    ) {
        if (journalDay == null) return;
        final VacationDayInfo info = vacationDayMap != null
                ? vacationDayMap.get(journalDay.getStdrdDt())
                : null;
        if (info == null) {
            journalDay.setVacationDayStatus(VacationDayStatus.NONE);
            journalDay.setVacationReasonList(List.of());
            return;
        }
        journalDay.setVacationDayStatus(info.getStatus());
        journalDay.setVacationReasonList(info.getReasonList() != null ? List.copyOf(info.getReasonList()) : List.of());
    }
}
