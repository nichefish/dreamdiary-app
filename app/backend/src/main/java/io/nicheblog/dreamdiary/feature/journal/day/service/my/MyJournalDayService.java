package io.nicheblog.dreamdiary.feature.journal.day.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam;
import io.nicheblog.dreamdiary.feature.journal.day.service.JournalDayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * MyJournalDayService
 * <pre>
 *  로그인 사용자 기준 저널 일자 서비스
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJournalDayService {

    private final JournalDayService journalDayService;

    /**
     * 내 년월 목록 조회 (dto level)
     *
     * @param yy 년도
     * @param mnth 월
     * @return {@link List} -- 조회된 목록
     */
    public List<JournalDayDto> getMyCachedYyMnthListDto(final Integer yy, final Integer mnth) throws Exception {
        final String loginUsername = AuthUtils.requireLoginUsername();
        return journalDayService.getCachedYyMnthListDtoByUser(loginUsername, yy, mnth);
    }

    /**
     * 현기준일자 목록 조회 (dto level)
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    @Transactional(readOnly = true)
    public List<JournalDayDto> getMyJournalStdrdDays(final JournalDaySearchParam searchParam) throws Exception {
        if (searchParam == null) return List.of();

        final String username = AuthUtils.requireLoginUsername();
        return journalDayService.getJournalStdrdDaysByUser(username, searchParam);
    }

    /**
     * 현주간 일자 목록 조회 (dto level) :: 캐시 처리
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    public List<JournalDayDto> getMyCachedWeeklyListDto(final JournalDaySearchParam searchParam) throws Exception {
        if (searchParam == null) return List.of();

        final String loginUsername = AuthUtils.requireLoginUsername();
        final String weekStartDt = StringUtils.isNotBlank(searchParam.getWeekStartDt())
                ? searchParam.getWeekStartDt()
                : io.nicheblog.dreamdiary.global.util.date.DateUtils.getWeekStartDateStr(searchParam.getStdrdDt());
        if (StringUtils.isBlank(weekStartDt)) return List.of();
        searchParam.setWeekStartDt(weekStartDt);

        return journalDayService.getCachedWeeklyListDtoByUser(loginUsername, weekStartDt);
    }

    /**
     * 메타번호별 일자 목록 조회 (dto level)
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    @Transactional(readOnly = true)
    public List<JournalDayDto> getMyListDtoByMetaId(final JournalDaySearchParam searchParam) throws Exception {
        if (searchParam == null) return List.of();

        final String username = AuthUtils.requireLoginUsername();
        return journalDayService.getListDtoByMetaIdAndUser(username, searchParam);
    }

    /**
     * 태그 ID별 일자 목록 조회 (dto level)
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 검색 결과 목록
     */
    @Transactional(readOnly = true)
    public List<JournalDayDto> getMyListDtoByTagId(final JournalDaySearchParam searchParam) throws Exception {
        if (searchParam == null) return List.of();

        final String username = AuthUtils.requireLoginUsername();
        return journalDayService.getListDtoByTagIdAndUser(username, searchParam);
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 일련번호
     * @return {@link JournalDayDto} -- 조회된 객체
     */
    public JournalDayDto getMyCachedDtlDto(final Integer key) throws Exception {
        final String loginUsername = AuthUtils.requireLoginUsername();
        return journalDayService.getCachedDtlDtoByUser(loginUsername, key);
    }

    /**
     * 중복 체크 (정상은 true / 중복은 false)
     *
     * @param journalDay 중복 여부를 확인할 객체
     * @return {@link boolean} -- 정상은 true, 중복은 false 반환
     */
    public boolean dupChck(final JournalDayDto journalDay) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return journalDayService.dupChckByUser(username, journalDay);
    }

    /**
     * 날짜 기준으로 중복된 기존 게시글 번호 반환
     *
     * @param journalDay 중복 여부를 확인할 객체
     * @return {@link Integer} -- 중복되는 경우 해당 게시글 번호
     */
    public Integer getDupKey(final JournalDayDto journalDay) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return journalDayService.getDupKeyByUser(username, journalDay);
    }
}

