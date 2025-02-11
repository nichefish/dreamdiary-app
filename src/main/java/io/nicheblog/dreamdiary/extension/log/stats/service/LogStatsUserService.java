package io.nicheblog.dreamdiary.extension.log.stats.service;

import io.nicheblog.dreamdiary.extension.log.stats.mapstruct.LogStatsUserMapstruct;
import io.nicheblog.dreamdiary.extension.log.stats.model.LogStatsUserDto;
import io.nicheblog.dreamdiary.extension.log.stats.model.LogStatsUserIntrfc;
import io.nicheblog.dreamdiary.extension.log.stats.repository.jpa.LogStatsUserRepository;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * LogStatsUserService
 * <pre>
 *  로그 통계 (사용자별) 서비스 모듈
 * </pre>
 *
 * @author nichefish
 */
@Service("logStatsUserService")
@RequiredArgsConstructor
public class LogStatsUserService {

    private final LogStatsUserRepository logStatsUserRepository;
    private final LogStatsUserMapstruct logStatsUserMapstruct = LogStatsUserMapstruct.INSTANCE;

    /**
     * 로그 통계 목록 조회 (entity level)
     *
     * @param searchParamMap 검색 조건이 담긴 파라미터 맵
     * @param pageable 페이지 정보 (페이징 처리)
     * @return {@link List} -- 조회된 로그 통계 목록
     * @throws Exception 검색 및 조회 중 발생할 수 있는 예외
     */
    @Transactional(readOnly = true)
    public List<LogStatsUserIntrfc> getStatsUserIntrfcList(
            final Map<String, Object> searchParamMap,
            final Pageable pageable
    ) throws Exception {
        // 목록 검색 (기본 조건 :: 당일 시작 ~ 당일 끝)
        if (!searchParamMap.containsKey("searchStartDt")) searchParamMap.put("searchStartDt", DateUtils.getCurrDateStr(DatePtn.DATE));
        // searchParamMap에서 빈 값들 및 쓸모없는 값들 정리
        Map<String, Object> filteredSearchKey = CmmUtils.Param.filterParamMap(searchParamMap);
        return logStatsUserRepository.getStatsUserIntrfcList((Date) filteredSearchKey.get("searchStartDt"), (Date) filteredSearchKey.get("searchEndDt"));
    }

    /**
     * 활동 로그 > 활동 로그 목록 조회 (dto level)
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @param pageable 페이지 정보 (페이징 처리)
     * @return {@link List} -- 조회된 활동 로그 목록
     * @throws Exception 검색 및 조회 중 발생할 수 있는 예외
     */
    @Transactional(readOnly = true)
    public List<LogStatsUserDto> logStatsUserDtoList(
            final BaseSearchParam searchParam,
            final Pageable pageable
    ) throws Exception {

        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);
        final List<LogStatsUserIntrfc> intrfcList = this.getStatsUserIntrfcList(searchParamMap, pageable);

        final List<LogStatsUserDto> dtoList = new ArrayList<>();
        for (final LogStatsUserIntrfc intrfc : intrfcList) {
            dtoList.add(logStatsUserMapstruct.toDto(intrfc));
        }
        dtoList.sort(Comparator.naturalOrder());
        long i = dtoList.size();
        for (LogStatsUserDto dto : dtoList) {
            dto.setRnum(i--);
        }
        return dtoList;
    }

    /**
     * 비로그인 유저 구분별로 건수 조회
     *
     * @param searchParamMap 검색 파라미터 맵
     * @return {@link List<LogStatsUserIntrfc>} 검색된 통계 목록
     * @throws Exception 검색 중 발생할 수 있는 예외
     */
    @Transactional(readOnly = true)
    public List<LogStatsUserIntrfc> getStatsNotUserIntrfcList(final Map<String, Object> searchParamMap, final Pageable pageable) throws Exception {
        // 목록 검색 (기본 조건 :: 당일 시작 ~ 당일 끝)
        if (!searchParamMap.containsKey("searchStartDt")) searchParamMap.put("searchStartDt", DateUtils.getCurrDateStr(DatePtn.DATE));
        // searchParamMap에서 빈 값들 및 쓸모없는 값들 정리
        final Map<String, Object> filteredSearchKey = CmmUtils.Param.filterParamMap(searchParamMap);
        return logStatsUserRepository.getStatsNotUserIntrfcList((Date) filteredSearchKey.get("searchStartDt"), (Date) filteredSearchKey.get("searchEndDt"));
    }

    /**
     * 비로그인 유저 구분별로 건수 조회
     *
     * @param searchParam 검색 파라미터
     * @param pageable 페이지 정보 (페이징 처리)
     * @return {@link List<LogStatsUserIntrfc>} 검색된 통계 목록
     * @throws Exception 검색 중 발생할 수 있는 예외
     */
    @Transactional(readOnly = true)
    public List<LogStatsUserDto> logStatsNotUserDtoList(final BaseSearchParam searchParam, final Pageable pageable) throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);
        final List<LogStatsUserIntrfc> intrfcList = this.getStatsNotUserIntrfcList(searchParamMap, pageable);

        final List<LogStatsUserDto> dtoList = new ArrayList<>();
        for (LogStatsUserIntrfc intrfc : intrfcList) {
            dtoList.add(logStatsUserMapstruct.toDto(intrfc));
        }
        dtoList.sort(Comparator.naturalOrder());
        return dtoList;
    }
}