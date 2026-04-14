package io.nicheblog.dreamdiary.feature.jrnl.day.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.day.service.JrnlDayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * MyJrnlDayService
 * <pre>
 *  로그인 사용자 기준 저널 일자 서비스
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJrnlDayService {

    private final JrnlDayService jrnlDayService;

    /**
     * 내 년월 목록 조회 (dto level)
     *
     * @param yy 년도
     * @param mnth 월
     * @return {@link List} -- 조회된 목록
     */
    public List<JrnlDayDto> getMyCachedYyMnthListDto(final Integer yy, final Integer mnth) throws Exception {
        final String lgnUsername = AuthUtils.requireLgnUsername();
        return jrnlDayService.getCachedYyMnthListDtoByUser(lgnUsername, yy, mnth);
    }

    /**
     * 현기준일자 목록 조회 (dto level)
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    @Transactional(readOnly = true)
    public List<JrnlDayDto> getMyJrnlStdrdDays(final JrnlDaySearchParam searchParam) throws Exception {
        if (searchParam == null) return List.of();

        final String username = AuthUtils.requireLgnUsername();
        return jrnlDayService.getJrnlStdrdDaysByUser(username, searchParam);
    }

    /**
     * 현주간 일자 목록 조회 (dto level) :: 캐시 처리
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    public List<JrnlDayDto> getMyCachedWeeklyListDto(final JrnlDaySearchParam searchParam) throws Exception {
        if (searchParam == null) return List.of();

        final String lgnUsername = AuthUtils.requireLgnUsername();
        final String weekStartDt = StringUtils.isNotBlank(searchParam.getWeekStartDt())
                ? searchParam.getWeekStartDt()
                : io.nicheblog.dreamdiary.global.util.date.DateUtils.getWeekStartDateStr(searchParam.getStdrdDt());
        if (StringUtils.isBlank(weekStartDt)) return List.of();
        searchParam.setWeekStartDt(weekStartDt);

        return jrnlDayService.getCachedWeeklyListDtoByUser(lgnUsername, weekStartDt);
    }

    /**
     * 메타번호별 일자 목록 조회 (dto level)
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    @Transactional(readOnly = true)
    public List<JrnlDayDto> getMyListDtoByMetaId(final JrnlDaySearchParam searchParam) throws Exception {
        if (searchParam == null) return List.of();

        final String username = AuthUtils.requireLgnUsername();
        return jrnlDayService.getListDtoByMetaIdAndUser(username, searchParam);
    }

    /**
     * 태그 ID별 일자 목록 조회 (dto level)
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 검색 결과 목록
     */
    @Transactional(readOnly = true)
    public List<JrnlDayDto> getMyListDtoByTagId(final JrnlDaySearchParam searchParam) throws Exception {
        if (searchParam == null) return List.of();

        final String username = AuthUtils.requireLgnUsername();
        return jrnlDayService.getListDtoByTagIdAndUser(username, searchParam);
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 일련번호
     * @return {@link JrnlDayDto} -- 조회된 객체
     */
    public JrnlDayDto getMyCachedDtlDto(final Integer key) throws Exception {
        final String lgnUsername = AuthUtils.requireLgnUsername();
        return jrnlDayService.getCachedDtlDtoByUser(lgnUsername, key);
    }

    /**
     * 중복 체크 (정상은 true / 중복은 false)
     *
     * @param jrnlDay 중복 여부를 확인할 객체
     * @return {@link boolean} -- 정상은 true, 중복은 false 반환
     */
    public boolean dupChck(final JrnlDayDto jrnlDay) throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        return jrnlDayService.dupChckByUser(username, jrnlDay);
    }

    /**
     * 날짜 기준으로 중복된 기존 게시글 번호 반환
     *
     * @param jrnlDay 중복 여부를 확인할 객체
     * @return {@link Integer} -- 중복되는 경우 해당 게시글 번호
     */
    public Integer getDupKey(final JrnlDayDto jrnlDay) throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        return jrnlDayService.getDupKeyByUser(username, jrnlDay);
    }
}
