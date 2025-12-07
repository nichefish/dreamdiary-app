package io.nicheblog.dreamdiary.domain.vcatn.stats.service;

import io.nicheblog.dreamdiary.domain.vcatn.stats.entity.VcatnStatsYyEntity;
import io.nicheblog.dreamdiary.domain.vcatn.stats.mapstruct.VcatnStatsYyMapstruct;
import io.nicheblog.dreamdiary.domain.vcatn.stats.model.VcatnStatsYyDto;
import io.nicheblog.dreamdiary.domain.vcatn.stats.repository.jpa.VcatnStatsYyRepository;
import io.nicheblog.dreamdiary.domain.vcatn.stats.spec.VcatnStatsYySpec;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * VcatnStatsYyService
 * <pre>
 *  휴가 기준년도 관리 서비스 모듈
 * </pre>
 *
 * @author nichefish
 */
@Service("vcatnStatsYyService")
@RequiredArgsConstructor
@Log4j2
public class VcatnStatsYyService {

    private final VcatnStatsYySpec vcatnStatsYySpec;
    private final VcatnStatsYyRepository vcatnStatsYyRepository;

    /**
     * 휴가관리 > 휴가 기준 년도 등록
     * 매년 1월 1일에 등록함 (0301~0228)
     *
     * @return {@link Boolean} -- 등록 성공 여부
     */
    @Transactional
    public Boolean regVcatnYyDt() throws Exception {
        // 매년 1월1일에 다음단위(0301~0228)것 등록
        final String yyStr = DateUtils.getCurrYyStr();
        final Date startDt = DateUtils.asDate(yyStr + "0301");
        final String nextYyStr = Integer.toString(DateUtils.getCurrYy() + 1);
        final Date endDt = DateUtils.getDateAddDay(DateUtils.asDate(nextYyStr + "0301"), -1);
        final VcatnStatsYyEntity entity = new VcatnStatsYyEntity(yyStr, startDt, endDt);
        vcatnStatsYyRepository.save(entity);

        return true;
    }

    /**
     * 휴가관리 > 휴가 기준 년도 조회 (entity level)
     * DB에 입력된 값에 대해서 조회 :: 입력값이 없을 경우 일반적으로 1월 1일 ~ 12월 31일로 처리한다.
     *
     * @param yyStr 조회할 기준 년도 문자열
     * @return {@link VcatnStatsYyEntity} -- 휴가 기준 년도 정보
     */
    public VcatnStatsYyEntity getVcatnYyDtEntity(final String yyStr) throws Exception {
        final Optional<VcatnStatsYyEntity> statsYyWrapper = vcatnStatsYyRepository.findById(yyStr);

        return statsYyWrapper.orElse(new VcatnStatsYyEntity(yyStr));
    }

    /**
     * 휴가관리 > 휴가 기준 년도 조회 (entity level)
     * DB에 입력된 값에 대해서 조회 :: 입력값이 없을 경우 일반적으로 1월 1일 ~ 12월 31일로 처리한다.
     *
     * @param yyStr 조회할 기준 년도 문자열. 부재시 현재 기준 년도를 조회합니다.
     * @return {@link VcatnStatsYyDto} -- 휴가 기준 년도 객체
     */
    @Transactional(readOnly = true)
    public VcatnStatsYyDto getVcatnYyDtDto(final String yyStr) throws Exception {
        if (StringUtils.isEmpty(yyStr)) return this.getCurrVcatnYyDt();

        return VcatnStatsYyMapstruct.INSTANCE.toDto(this.getVcatnYyDtEntity(yyStr));
    }

    /**
     * 휴가관리 > 휴가 기준 년도 시작/종료일자가 담긴 Map 반환
     *
     * @param statsYy 휴가 기준 년도 정보
     * @return {@link Map} -- 시작일자와 종료일자가 담긴 Map
     */
    public Map<String, Object> getVcatnYyDtMap(final VcatnStatsYyDto statsYy) throws Exception {
        if (statsYy == null) return null;

        return new HashMap<>() {{
            put("searchStartDt", DateUtils.asDate(statsYy.getBgnDt()));
            put("searchEndDt", DateUtils.asDate(statsYy.getEndDt()));
        }};
    }

    /**
     * 휴가관리 > 오늘 날짜가 속한 휴가 기준 정보 반환
     *
     * @return {@link VcatnStatsYyDto} -- 오늘 날짜가 속한 VcatnStatsYyDto 객체
     */
    @Transactional(readOnly = true)
    public VcatnStatsYyDto getCurrVcatnYyDt() throws Exception {
        final Map<String, Object> searchParamMap = new HashMap<>() {{
            put("today", DateUtils.getCurrDate());
        }};
        final Page<VcatnStatsYyEntity> statsYyPage = vcatnStatsYyRepository.findAll(vcatnStatsYySpec.searchWith(searchParamMap), Pageable.unpaged());
        final List<VcatnStatsYyEntity> statsYyList = statsYyPage.getContent();
        if (CollectionUtils.isEmpty(statsYyList)) return null;
        final VcatnStatsYyEntity statsYy = statsYyList.get(0);

        return VcatnStatsYyMapstruct.INSTANCE.toDto(statsYy);
    }
}
