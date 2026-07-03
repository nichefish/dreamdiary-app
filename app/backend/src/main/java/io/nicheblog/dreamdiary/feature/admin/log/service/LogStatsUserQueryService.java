package io.nicheblog.dreamdiary.feature.admin.log.service;

import io.nicheblog.dreamdiary.feature.admin.log.mapstruct.LogStatsUserReadMapstruct;
import io.nicheblog.dreamdiary.feature.admin.log.model.LogStatsUserQueryDto;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.log.stats.model.LogStatsSearchParam;
import io.nicheblog.dreamdiary.infrastructure.log.stats.model.LogStatsUserIntrfc;
import io.nicheblog.dreamdiary.infrastructure.log.stats.repository.jpa.LogStatsUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * LogStatsUserQueryService
 * <pre>
 *  사용자별 로그 통계 조회 전용 서비스.
 * </pre>
 *
 * 변경 전(레거시): Map 파라미터 + CmmUtils.filterParamMap 으로 날짜 문자열을 변환했다.
 * 변경 후: LogStatsSearchParam 을 직접 받아 기간을 해석한다. (기본 노출 = 오늘 통계, 레거시 동일)
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class LogStatsUserQueryService {

    private final LogStatsUserRepository logStatsUserRepository;
    private final LogStatsUserReadMapstruct logStatsUserReadMapstruct = LogStatsUserReadMapstruct.INSTANCE;

    /**
     * 로그인 사용자별 로그 통계 목록 조회
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link List} -- 로그 수 내림차순 정렬·순번 부여된 통계 목록
     */
    @Transactional(readOnly = true)
    public List<LogStatsUserQueryDto> getStatsUserDtoList(final LogStatsSearchParam searchParam) throws Exception {
        final Date[] range = this.resolveSearchRange(searchParam);
        return this.toSortedDtoList(logStatsUserRepository.getStatsUserIntrfcList(range[0], range[1]));
    }

    /**
     * 비로그인 사용자 구분별 로그 통계 목록 조회
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link List} -- 로그 수 내림차순 정렬·순번 부여된 통계 목록
     */
    @Transactional(readOnly = true)
    public List<LogStatsUserQueryDto> getStatsNotUserDtoList(final LogStatsSearchParam searchParam) throws Exception {
        final Date[] range = this.resolveSearchRange(searchParam);
        return this.toSortedDtoList(logStatsUserRepository.getStatsNotUserIntrfcList(range[0], range[1]));
    }

    /**
     * 조회 기간을 해석한다. (시작일 미지정 시 오늘 — 기본 노출은 '오늘의 활동 통계', 레거시 동일.
     * 종료일 미지정 시 시작일과 같은 날의 끝 시간까지.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link Date} 배열 -- [시작 시간, 끝 시간]
     */
    private Date[] resolveSearchRange(final LogStatsSearchParam searchParam) throws Exception {
        final String startStr = StringUtils.defaultIfEmpty(searchParam.getSearchStartDt(), DateUtils.getCurrDateStr(DatePtn.DATE));
        final String endStr = StringUtils.defaultIfEmpty(searchParam.getSearchEndDt(), startStr);
        return new Date[]{ DateUtils.Parser.sDateParse(startStr), DateUtils.Parser.eDateParse(endStr) };
    }

    /**
     * 조회 결과를 DTO 로 변환하고 로그 수 내림차순 정렬 + 순번(rnum) 부여한다. (레거시 logStatsUserDtoList 동일)
     *
     * @param intrfcList 조회된 통계 인터페이스 목록
     * @return {@link List} -- 정렬·순번 부여된 DTO 목록
     */
    private List<LogStatsUserQueryDto> toSortedDtoList(final List<LogStatsUserIntrfc> intrfcList) throws Exception {
        final List<LogStatsUserQueryDto> dtoList = new ArrayList<>();
        for (final LogStatsUserIntrfc intrfc : intrfcList) {
            dtoList.add(logStatsUserReadMapstruct.toDto(intrfc));
        }
        dtoList.sort(Comparator.naturalOrder());
        long i = dtoList.size();
        for (final LogStatsUserQueryDto dto : dtoList) {
            dto.setRnum(i--);
        }
        return dtoList;
    }
}
