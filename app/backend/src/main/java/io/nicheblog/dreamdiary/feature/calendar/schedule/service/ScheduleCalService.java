package io.nicheblog.dreamdiary.feature.calendar.schedule.service;

import io.nicheblog.dreamdiary.feature.calendar.schedule.entity.ScheduleEntity;
import io.nicheblog.dreamdiary.feature.calendar.schedule.mapstruct.ScheduleCalMapstruct;
import io.nicheblog.dreamdiary.feature.calendar.schedule.model.ScheduleCalDto;
import io.nicheblog.dreamdiary.feature.calendar.schedule.model.ScheduleSearchParam;
import io.nicheblog.dreamdiary.feature.calendar.schedule.repository.jpa.ScheduleRepository;
import io.nicheblog.dreamdiary.feature.calendar.schedule.spec.ScheduleSpec;
import io.nicheblog.dreamdiary.global.intrfc.model.fullcalendar.BaseCalDto;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

    private final ScheduleService scheduleService;
    private final ScheduleCalMapstruct scheduleCalMapstruct = ScheduleCalMapstruct.INSTANCE;
    private final ScheduleSpec scheduleSpec;
    private final ScheduleRepository scheduleRepository;

    /**
     * 일정 > 전체 일정 (일정 및 휴가) 데이터 조회
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 조회된 일정 및 휴가 목록
     */
    public List<BaseCalDto> getScheduleTotalCalList(final ScheduleSearchParam searchParam) throws Exception {
        final List<BaseCalDto> totalScheduleCalList = new ArrayList<>();

        // 생일 달력 목록 검색
        // final List<ScheduleCalDto> brthdyCalList = this.getBrthdyCalList(searchParam);
        // totalScheduleCalList.addAll(brthdyCalList);

        // 일정(공휴일, 행사) 달력 목록 검색
        final List<BaseCalDto> holydayCalList = this.getHolydayCalList(searchParam);
        totalScheduleCalList.addAll(holydayCalList);

        // 일정(재택근무, 외근) 달력 목록 검색
        final List<ScheduleCalDto> scheduleCalList = this.getScheduleCalList(searchParam);
        totalScheduleCalList.addAll(scheduleCalList);

        // 개인 일정 달력 목록 검색
        final boolean prvtChked = "Y".equals(searchParam.getPrvtChked());
        if (prvtChked) {
            final List<ScheduleCalDto> prvtCalList = this.getPrvtCalList(searchParam);
            totalScheduleCalList.addAll(prvtCalList);
        }

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
        final Map<String, Object> searchParamMap = new HashMap<>() {{
            put("searchStartDt", searchParam.getSearchStartDt());
            put("searchEndDt", searchParam.getSearchEndDt());
            put("getHolydayCeremonyOnly", true);
        }};

        // 일정 목록 검색
        final List<ScheduleEntity> scheduleEntityList = scheduleRepository.findAll(scheduleSpec.searchWith(searchParamMap));
        if (scheduleEntityList.isEmpty()) return Collections.emptyList();

        // entity -> dto
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

    /**
     * 일정(재택근무, 내근, 외근) 달력 목록 검색
     *
     * @param searchParam 일정 검색 파라미터
     * @return {@link List} 일정 목록 반환
     */
    public List<ScheduleCalDto> getScheduleCalList(final ScheduleSearchParam searchParam) throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);
        final Map<String, Object> filteredSearchKey = CmmUtils.filterParamMap(searchParamMap);
        // 일정 목록 검색
        final List<ScheduleEntity> scheduleEntityList = scheduleRepository.findAll(scheduleSpec.searchWith(filteredSearchKey));
        if (scheduleEntityList.isEmpty()) return Collections.emptyList();

        return ScheduleCalMapstruct.INSTANCE.toDtoList(scheduleEntityList);
    }

    /**
     * 개인 일정 달력 목록 검색
     *
     * @param searchParam 일정 검색 파라미터
     * @return {@link List} -- 개인 일정 달력 목록 Dto 리스트
     */
    public List<ScheduleCalDto> getPrvtCalList(final ScheduleSearchParam searchParam) throws Exception {
        searchParam.setPrevOnly(true);
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);
        final Map<String, Object> filteredSearchKey = CmmUtils.filterParamMap(searchParamMap);

        // 일정 목록 검색
        final List<ScheduleEntity> scheduleEntityList = scheduleRepository.findAll(scheduleSpec.searchWith(filteredSearchKey));
        if (scheduleEntityList.isEmpty()) return Collections.emptyList();

        return ScheduleCalMapstruct.INSTANCE.toDtoList(scheduleEntityList);
    }

}

