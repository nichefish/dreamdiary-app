package io.nicheblog.dreamdiary.feature.jrnl.day.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.day.service.JrnlDayCalService;
import io.nicheblog.dreamdiary.feature.schdul.service.SchdulCalService;
import io.nicheblog.dreamdiary.global.intrfc.model.fullcalendar.BaseCalDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * MyJrnlDayCalService
 * <pre>
 *  로그인 사용자 기준 저널 일자 달력 서비스
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJrnlDayCalService {

    private final JrnlDayCalService jrnlDayCalService;
    private final SchdulCalService schdulCalService;

    /**
     * 전체 목록 (저널일자 및 일정) 데이터 조회
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 조회된 일정 및 일기 목록
     */
    public List<BaseCalDto> getSchdulTotalCalList(final JrnlDaySearchParam searchParam) throws Exception {

        // 저널일자 캘린더 목록 조회
        final List<BaseCalDto> jrnlDayCalList = this.getMyCalListDto(searchParam);
        final List<BaseCalDto> totalSchdulCalList = new ArrayList<>(jrnlDayCalList);

        // 일정(공휴일, 행사) 캘린더 목록 검색
        final List<BaseCalDto> hldyCalList = schdulCalService.getHldyCalList(searchParam);
        totalSchdulCalList.addAll(hldyCalList);

        return totalSchdulCalList;
    }

    /**
     * 달력목록 조회 (dto level) :: 캐시 처리
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    public List<BaseCalDto> getMyCalListDto(final JrnlDaySearchParam searchParam) throws Exception {
        final String userId = AuthUtils.requireUserId(AuthUtils.getLgnUserId());
        return jrnlDayCalService.getCalListDtoByUser(userId, searchParam);
    }
}
