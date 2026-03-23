package io.nicheblog.dreamdiary.feature.admin.log.stats.service;

import io.nicheblog.dreamdiary.feature.admin.log.stats.mapstruct.LogStatsUserReadMapstruct;
import io.nicheblog.dreamdiary.feature.admin.log.stats.model.LogStatsUserQueryDto;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.log.stats.model.LogStatsUserIntrfc;
import io.nicheblog.dreamdiary.infrastructure.log.stats.repository.jpa.LogStatsUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * LogStatsUserQueryService
 * <pre>
 *  로그 통계 조회 전용 서비스.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
public class LogStatsUserQueryService {

    private final LogStatsUserRepository logStatsUserRepository;
    private final LogStatsUserReadMapstruct logStatsUserReadMapstruct = LogStatsUserReadMapstruct.INSTANCE;

    @Transactional(readOnly = true)
    public List<LogStatsUserIntrfc> getStatsUserIntrfcList(
            final Map<String, Object> searchParamMap,
            final Pageable pageable
    ) throws Exception {
        if (!searchParamMap.containsKey("searchStartDt")) searchParamMap.put("searchStartDt", DateUtils.getCurrDateStr(DatePtn.DATE));
        final Map<String, Object> filteredSearchKey = CmmUtils.filterParamMap(searchParamMap);
        return logStatsUserRepository.getStatsUserIntrfcList((Date) filteredSearchKey.get("searchStartDt"), (Date) filteredSearchKey.get("searchEndDt"));
    }

    @Transactional(readOnly = true)
    public List<LogStatsUserQueryDto> logStatsUserDtoList(
            final BaseSearchParam searchParam,
            final Pageable pageable
    ) throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);
        final List<LogStatsUserIntrfc> intrfcList = this.getStatsUserIntrfcList(searchParamMap, pageable);

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

    @Transactional(readOnly = true)
    public List<LogStatsUserIntrfc> getStatsNotUserIntrfcList(
            final Map<String, Object> searchParamMap,
            final Pageable pageable
    ) throws Exception {
        if (!searchParamMap.containsKey("searchStartDt")) searchParamMap.put("searchStartDt", DateUtils.getCurrDateStr(DatePtn.DATE));
        final Map<String, Object> filteredSearchKey = CmmUtils.filterParamMap(searchParamMap);
        return logStatsUserRepository.getStatsNotUserIntrfcList((Date) filteredSearchKey.get("searchStartDt"), (Date) filteredSearchKey.get("searchEndDt"));
    }

    @Transactional(readOnly = true)
    public List<LogStatsUserQueryDto> logStatsNotUserDtoList(
            final BaseSearchParam searchParam,
            final Pageable pageable
    ) throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);
        final List<LogStatsUserIntrfc> intrfcList = this.getStatsNotUserIntrfcList(searchParamMap, pageable);

        final List<LogStatsUserQueryDto> dtoList = new ArrayList<>();
        for (final LogStatsUserIntrfc intrfc : intrfcList) {
            dtoList.add(logStatsUserReadMapstruct.toDto(intrfc));
        }
        dtoList.sort(Comparator.naturalOrder());
        return dtoList;
    }
}
