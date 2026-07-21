package io.nicheblog.dreamdiary.feature.calendar.schedule.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.calendar.schedule.model.VacationDayInfo;
import io.nicheblog.dreamdiary.feature.calendar.schedule.model.VacationScheduleProjection;
import io.nicheblog.dreamdiary.feature.calendar.schedule.repository.jpa.ScheduleRepository;
import io.nicheblog.dreamdiary.feature.calendar.schedule.type.VacationDayStatus;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 현재 사용자가 참가한 휴가 일정을 저널 일자별 상태로 투영한다.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class ScheduleVacationQueryService {

    private final ScheduleRepository scheduleRepository;

    /**
     * 요청 일자 전체를 포함하는 최소 범위로 휴가 일정을 한 번 조회하고, 요청된 날짜에만 펼친다.
     * 변경 전에는 저널 일자 응답에 사용자 휴가 정보가 없었고 공휴일·주말 정보만 존재했다.
     * 변경 후에는 전역 휴일 축을 변경하지 않고 현재 사용자 참가 휴가만 별도 맵으로 반환한다.
     *
     * @param username 사용자 계정명
     * @param dateValues 투영할 ISO 날짜 문자열 목록
     * @return 날짜별 사용자 휴가 정보
     */
    @Transactional(readOnly = true)
    public Map<String, VacationDayInfo> getVacationDayMapByUser(
            final String username,
            final Collection<String> dateValues
    ) {
        final String resolvedUsername = AuthUtils.requireUsername(username);
        if (dateValues == null || dateValues.isEmpty()) return Map.of();

        final List<LocalDate> requestedDates = dateValues.stream()
                .filter(StringUtils::isNotBlank)
                .map(this::parseDate)
                .distinct()
                .sorted()
                .toList();
        if (requestedDates.isEmpty()) return Map.of();

        final LocalDate rangeStart = requestedDates.get(0);
        final LocalDate rangeEnd = requestedDates.get(requestedDates.size() - 1);
        final List<VacationScheduleProjection> scheduleList = scheduleRepository.findParticipantSchedulesOverlapping(
                resolvedUsername,
                Code.SCHEDULE_VCATN,
                rangeStart.atStartOfDay(),
                rangeEnd.plusDays(1).atStartOfDay()
        );
        if (scheduleList == null || scheduleList.isEmpty()) return Map.of();

        final Set<LocalDate> requestedDateSet = new HashSet<>(requestedDates);
        final Map<LocalDate, MutableVacationDayInfo> mutableInfoMap = new LinkedHashMap<>();
        for (final VacationScheduleProjection schedule : scheduleList) {
            this.mergeSchedule(schedule, rangeStart, rangeEnd, requestedDateSet, mutableInfoMap);
        }

        final Map<String, VacationDayInfo> result = new LinkedHashMap<>();
        mutableInfoMap.forEach((date, info) -> result.put(
                date.toString(),
                VacationDayInfo.builder()
                        .status(info.status)
                        .reasonList(List.copyOf(info.reasonSet))
                        .build()
        ));
        return result;
    }

    private void mergeSchedule(
            final VacationScheduleProjection schedule,
            final LocalDate rangeStart,
            final LocalDate rangeEnd,
            final Set<LocalDate> requestedDateSet,
            final Map<LocalDate, MutableVacationDayInfo> mutableInfoMap
    ) {
        if (schedule == null || schedule.getBgnDt() == null) {
            log.warn("SCHEDULE_VACATION_PROJECTION_SKIPPED reason=missing-start scheduleId={}",
                    schedule != null ? schedule.getId() : null);
            return;
        }

        final LocalDate scheduleStart = schedule.getBgnDt().toLocalDate();
        final LocalDate scheduleEnd = schedule.getEndDt() != null
                ? schedule.getEndDt().toLocalDate()
                : scheduleStart;
        if (scheduleEnd.isBefore(scheduleStart)) {
            log.warn("SCHEDULE_VACATION_PROJECTION_SKIPPED reason=end-before-start scheduleId={} bgnDt={} endDt={}",
                    schedule.getId(), schedule.getBgnDt(), schedule.getEndDt());
            return;
        }

        final VacationDayStatus scheduleStatus = VacationDayStatus.fromVacationCode(schedule.getVcatnCd());
        if (scheduleStatus == VacationDayStatus.UNKNOWN) {
            log.warn("SCHEDULE_VACATION_STATUS_UNKNOWN scheduleId={} vcatnCd={}",
                    schedule.getId(), schedule.getVcatnCd());
        }

        LocalDate date = scheduleStart.isAfter(rangeStart) ? scheduleStart : rangeStart;
        final LocalDate lastDate = scheduleEnd.isBefore(rangeEnd) ? scheduleEnd : rangeEnd;
        while (!date.isAfter(lastDate)) {
            if (requestedDateSet.contains(date)) {
                final MutableVacationDayInfo info = mutableInfoMap.computeIfAbsent(
                        date,
                        ignored -> new MutableVacationDayInfo()
                );
                info.status = info.status.merge(scheduleStatus);
                if (StringUtils.isNotBlank(schedule.getTitle())) info.reasonSet.add(schedule.getTitle());
            }
            date = date.plusDays(1);
        }
    }

    private LocalDate parseDate(final String dateValue) {
        try {
            return LocalDate.parse(dateValue);
        } catch (final DateTimeParseException e) {
            log.warn("SCHEDULE_VACATION_PROJECTION_INVALID_DATE dateValue={}", dateValue);
            throw new IllegalArgumentException("invalid journal date: " + dateValue, e);
        }
    }

    /** 일자별 상태와 중복 제거된 표시 사유를 누적하는 내부 객체. */
    private static final class MutableVacationDayInfo {
        private VacationDayStatus status = VacationDayStatus.NONE;
        private final Set<String> reasonSet = new LinkedHashSet<>();
    }
}
