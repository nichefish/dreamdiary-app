package io.nicheblog.dreamdiary.feature.calendar.schedule.service;

import io.nicheblog.dreamdiary.feature.calendar.schedule.entity.ScheduleEntity;
import io.nicheblog.dreamdiary.feature.calendar.schedule.mapstruct.ScheduleCalMapstruct;
import io.nicheblog.dreamdiary.feature.calendar.schedule.mapstruct.ScheduleMapstruct;
import io.nicheblog.dreamdiary.feature.calendar.schedule.model.ScheduleCalDto;
import io.nicheblog.dreamdiary.feature.calendar.schedule.model.ScheduleDto;
import io.nicheblog.dreamdiary.feature.calendar.schedule.model.ScheduleSearchParam;
import io.nicheblog.dreamdiary.feature.calendar.schedule.repository.jpa.ScheduleRepository;
import io.nicheblog.dreamdiary.feature.calendar.schedule.spec.ScheduleSpec;
import io.nicheblog.dreamdiary.global.intrfc.model.fullcalendar.BaseCalDto;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ScheduleCalService
 * <pre>
 *  일정 달력 서비스 모듈
 * </pre>
 *
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class ScheduleCalService {

    private final ScheduleCalMapstruct scheduleCalMapstruct = ScheduleCalMapstruct.INSTANCE;
    private final ScheduleMapstruct scheduleMapstruct = ScheduleMapstruct.INSTANCE;
    private final ScheduleSpec scheduleSpec;
    private final ScheduleRepository scheduleRepository;

    /**
     * 달력/목록 API 공통: bgnDt·endDt 조회 구간을 searchStartDt·searchEndDt 로 맞춘다.
     *
     * @param searchParam 일정 검색 파라미터
     */
    private void applyCalendarDateRange(final ScheduleSearchParam searchParam) {
        if (searchParam == null) return;
        if (StringUtils.isNotBlank(searchParam.getBgnDt())) {
            searchParam.setSearchStartDt(searchParam.getBgnDt());
        }
        if (StringUtils.isNotBlank(searchParam.getEndDt())) {
            searchParam.setSearchEndDt(searchParam.getEndDt());
        }
        // 정규화 후 alias(bgnDt/endDt) 제거: convertToMap → filterParamMap 에서 "Dt" 로 끝나는 키가
        // java.util.Date 로 변환되어 ScheduleSpec default(equal) 로 새어들어가 LocalDateTime 필드와
        // 타입 불일치가 되는 것을 방지한다. (searchStartDt/searchEndDt 단일 경로로 수렴)
        searchParam.setBgnDt(null);
        searchParam.setEndDt(null);
    }

    /**
     * 일정 > 전체 일정 (일정 및 휴가) 데이터 조회
     * <p>변경 전에는 공휴일·행사, 일반 일정, 개인 일정을 각각 조회한 뒤 메모리에서 병합했다.</p>
     * <p>변경 후에는 공개 일정과 현재 사용자가 참가한 개인 일정을 단일 가시성 조건으로 조회한다.</p>
     * <p>변경 전 분리 경로의 의미: 생일 달력 목록 검색은 이미 비활성 상태였고,
     * 일정(공휴일, 행사) 달력 목록 검색·일정(재택근무, 외근) 달력 목록 검색·
     * 개인 일정 달력 목록 검색을 순서대로 더했다. 개인 일정 전용 {@code getPrvtCalList}도
     * 같은 단일 조회로 흡수하여 별도 공개 메서드를 남기지 않는다.</p>
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 조회된 일정 및 휴가 목록
     */
    public List<BaseCalDto> getScheduleTotalCalList(final ScheduleSearchParam searchParam) throws Exception {
        applyCalendarDateRange(searchParam);
        final List<BaseCalDto> totalScheduleCalList = scheduleEntityListToCalDto(findScheduleEntities(searchParam));

        totalScheduleCalList.sort(Comparator.naturalOrder());

        return totalScheduleCalList;
    }

    /**
     * 각 휴가에 대해서 휴가달력일정을 산정해서 목록에 추가
     *
     * @param vcatn 휴가 정보
     * @param vcatnCalList 산정 정보를 누적한 휴가달력일정
     */
    /*private void procVcatnCal(VcatnScheduleEntity vcatn, List<ScheduleCalDto> vcatnCalList) throws Exception {
        final Date vcatnEndDt = DateUtils.Parser.sDateParse(vcatn.getEndDt());

        // 로직 :: 날짜 훑으면서 각 일자별로 쪼갬. 공휴일 또는 주말여부 체크
        Date keyDt = DateUtils.Parser.sDateParse(vcatn.getBgnDt());
        // 1. 첫날 객체부터 시작
        ScheduleCalDto calDto = scheduleCalMapstruct.toCalDto(vcatn);
        boolean isDelimStart = true;
        assert (keyDt != null && vcatnEndDt != null);
        // 2. 주말/공휴일 제외하고 출력하는 로직 :: 하루씩 순회하며 주말여부 체크
        while (keyDt != null && keyDt.compareTo(vcatnEndDt) <= 0) {
            Boolean isHolyday = scheduleService.isHolydayOrWeekend(keyDt);
            boolean wasHolyday = scheduleService.isHolydayOrWeekend(DateUtils.getDateAddDay(keyDt, -1));
            boolean changedToHolyday = !wasHolyday && isHolyday;
            boolean isEndDt = DateUtils.isSameDay(keyDt, vcatnEndDt);

            if (!isHolyday) {
                // 휴일이 아니면? 날짜세기 시작
                if (isDelimStart) {
                    calDto = this.initNewCalDto(vcatn, keyDt);
                    isDelimStart = false;
                }
                // 끝일자면? 날짜세기 마무리
                if (isEndDt) {
                    this.finNewCalDto(calDto, keyDt);
                    vcatnCalList.add(calDto);
                    isDelimStart = true;
                }
            } else {
                // 휴일이면? 이전일자 기준으로 끊어간다.
                if (!isDelimStart && changedToHolyday) {
                    this.finNewCalDto(calDto, DateUtils.getDateAddDay(keyDt, -1));
                    vcatnCalList.add(calDto);
                    isDelimStart = true;
                }
            }
            // 위 조건에 해당 안할시? 다음날짜로 넘어간다.
            keyDt = DateUtils.getDateAddDay(keyDt, 1);
        }
    }*/

    /**
     * 일정 정보에서 keyDt에 대해 CalDto 마무리 :: 메소드 분리
     *
     * @param calDto 일정 정보를 담고 있는 ScheduleCalDto 객체
     * @param keyDt 일정의 키 날짜
     */
    private void finNewCalDto(final ScheduleCalDto calDto, final Date keyDt) throws Exception {
        calDto.setEndDt(DateUtils.Parser.eDateParseStr(keyDt));
        // 1일짜리 일정일 경우 : allday=true로 줘야 제대로 나온다.
        final boolean isSameDay = DateUtils.isSameDay(calDto.getBgnDt(), calDto.getEndDt());
        if (isSameDay) calDto.setAllDay(true);
    }

    /**
     * 일정(공휴일, 행사) 달력 목록 검색
     *
     * @param searchParam 일정 검색 파라미터
     * @return {@link List} 공휴일 및 행사 일정 목록을 반환
     */
    public List<BaseCalDto> getHolydayCalList(final BaseSearchParam searchParam) throws Exception {
        if (searchParam instanceof ScheduleSearchParam scheduleSearchParam) {
            applyCalendarDateRange(scheduleSearchParam);
        }
        return scheduleEntityListToCalDto(findHolydayEntities(searchParam));
    }

    /**
     * 일정 > 목록 VIEW (달력과 동일 필터·기간, ScheduleDto 페이징)
     *
     * @param searchParam 검색 조건
     * @param pageable 페이징
     * @return 일정 목록 페이지
     */
    public Page<ScheduleDto> getScheduleListPage(
            final ScheduleSearchParam searchParam,
            final Pageable pageable
    ) throws Exception {
        applyCalendarDateRange(searchParam);
        final Page<ScheduleEntity> entityPage = scheduleRepository.findAll(
                scheduleSpec.searchWith(buildVisibleSearchParamMap(searchParam)),
                pageable
        );
        return entityPage.map(entity -> {
            try {
                return scheduleMapstruct.toDto(entity);
            } catch (final Exception e) {
                throw new IllegalStateException("schedule dto mapping failed. id=" + entity.getId(), e);
            }
        });
    }

    private List<ScheduleEntity> findHolydayEntities(final BaseSearchParam searchParam) throws Exception {
        final Map<String, Object> searchParamMap = new HashMap<>() {{
            put("searchStartDt", searchParam.getSearchStartDt());
            put("searchEndDt", searchParam.getSearchEndDt());
            put("getHolydayCeremonyOnly", true);
        }};
        return scheduleRepository.findAll(scheduleSpec.searchWith(searchParamMap));
    }

    private List<BaseCalDto> scheduleEntityListToCalDto(final List<ScheduleEntity> scheduleEntityList) throws Exception {
        if (scheduleEntityList.isEmpty()) return Collections.emptyList();
        return scheduleEntityList.stream()
                .map(entity -> {
                    try {
                        return scheduleCalMapstruct.toCalDto(entity);
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    private List<ScheduleEntity> findScheduleEntities(final ScheduleSearchParam searchParam) throws Exception {
        return scheduleRepository.findAll(scheduleSpec.searchWith(buildVisibleSearchParamMap(searchParam)));
    }

    /**
     * 일정 화면 조회 파라미터를 단일 가시성 계약으로 변환한다.
     * 요청에서 개인 일정 선택값이 빠져도 공개 일정만 조회하도록 N을 강제한다.
     *
     * @param searchParam 일정 검색 조건
     * @return Specification 입력 맵
     */
    private Map<String, Object> buildVisibleSearchParamMap(final ScheduleSearchParam searchParam) throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);
        final Map<String, Object> filteredSearchKey = CmmUtils.filterParamMap(searchParamMap);
        filteredSearchKey.put("prvtChked", "Y".equals(searchParam.getPrvtChked()) ? "Y" : "N");
        return filteredSearchKey;
    }

    /**
     * 일정(재택근무, 내근, 외근) 달력 목록 검색
     *
     * @param searchParam 일정 검색 파라미터
     * @return {@link List} 일정 목록 반환
     */
    public List<ScheduleCalDto> getScheduleCalList(final ScheduleSearchParam searchParam) throws Exception {
        applyCalendarDateRange(searchParam);
        final List<ScheduleEntity> scheduleEntityList = findScheduleEntities(searchParam);
        if (scheduleEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        final List<ScheduleCalDto> dtoList = ScheduleCalMapstruct.INSTANCE.toDtoList(scheduleEntityList);
        return dtoList != null ? dtoList : Collections.emptyList();
    }

}
