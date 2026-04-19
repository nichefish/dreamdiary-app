package io.nicheblog.dreamdiary.feature.journal.day.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.calendar.schedule.service.ScheduleCalService;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam;
import io.nicheblog.dreamdiary.feature.journal.day.service.JournalDayCalService;
import io.nicheblog.dreamdiary.global.intrfc.model.fullcalendar.BaseCalDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * MyJournalDayCalService
 * <pre>
 *  로그인 사용자 기준 저널 일자 달력 서비스
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJournalDayCalService {

    private final JournalDayCalService journalDayCalService;
    private final ScheduleCalService scheduleCalService;

    /**
     * 전체 목록 (저널일자 및 일정) 데이터 조회
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 조회된 일정 및 일기 목록
     */
    public List<BaseCalDto> getScheduleTotalCalList(final JournalDaySearchParam searchParam) throws Exception {

        // 저널일자 캘린더 목록 조회
        final List<BaseCalDto> journalDayCalList = this.getMyCalListDto(searchParam);
        final List<BaseCalDto> totalScheduleCalList = new ArrayList<>(journalDayCalList);

        // 일정(공휴일, 행사) 캘린더 목록 검색
        final List<BaseCalDto> holydayCalList = scheduleCalService.getHolydayCalList(searchParam);
        totalScheduleCalList.addAll(holydayCalList);

        return totalScheduleCalList;
    }

    /**
     * 달력목록 조회 (dto level) :: 캐시 처리
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    public List<BaseCalDto> getMyCalListDto(final JournalDaySearchParam searchParam) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return journalDayCalService.getCalListDtoByUser(username, searchParam);
    }
}

