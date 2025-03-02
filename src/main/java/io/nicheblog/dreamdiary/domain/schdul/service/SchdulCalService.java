package io.nicheblog.dreamdiary.domain.schdul.service;

import io.nicheblog.dreamdiary.domain.schdul.entity.SchdulEntity;
import io.nicheblog.dreamdiary.domain.schdul.mapstruct.SchdulCalMapstruct;
import io.nicheblog.dreamdiary.domain.schdul.model.SchdulCalDto;
import io.nicheblog.dreamdiary.domain.schdul.model.SchdulSearchParam;
import io.nicheblog.dreamdiary.domain.schdul.repository.jpa.SchdulRepository;
import io.nicheblog.dreamdiary.domain.schdul.spec.SchdulSpec;
import io.nicheblog.dreamdiary.domain.vcatn.papr.entity.VcatnSchdulEntity;
import io.nicheblog.dreamdiary.domain.vcatn.papr.service.VcatnSchdulService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.intrfc.model.fullcalendar.BaseCalDto;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SchdulCalService
 * <pre>
 *  일정 달력 서비스 모듈
 * </pre>
 *
 *
 * @author nichefish
 */
@Service("schdulCalService")
@RequiredArgsConstructor
@Log4j2
public class SchdulCalService {

    private final SchdulService schdulService;
    private final VcatnSchdulService vcatnSchdulService;
    private final SchdulCalMapstruct schdulCalMapstruct = SchdulCalMapstruct.INSTANCE;
    private final SchdulSpec schdulSpec;
    private final SchdulRepository schdulRepository;

    /**
     * 일정 > 전체 일정 (일정 및 휴가) 데이터 조회
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 조회된 일정 및 휴가 목록
     * @throws Exception 조회 및 처리 중 발생할 수 있는 예외
     */
    public List<BaseCalDto> getSchdulTotalCalList(final SchdulSearchParam searchParam) throws Exception {
        final List<BaseCalDto> totalSchdulCalList = new ArrayList<>();

        // 휴가 달력 목록 검색
        final String vcatnChk = searchParam.getVcatnChked();
        if ("Y".equals(vcatnChk)) {
            final List<SchdulCalDto> vcatnCalList = this.getVcatnCalList(searchParam);
            totalSchdulCalList.addAll(vcatnCalList);
        }

        // 생일 달력 목록 검색
        // final List<SchdulCalDto> brthdyCalList = this.getBrthdyCalList(searchParam);
        // totalSchdulCalList.addAll(brthdyCalList);

        // 일정(공휴일, 행사) 달력 목록 검색
        final List<BaseCalDto> hldyCalList = this.getHldyCalList(searchParam);
        totalSchdulCalList.addAll(hldyCalList);

        // 일정(재택근무, 외근) 달력 목록 검색
        final List<SchdulCalDto> schdulCalList = this.getSchdulCalList(searchParam);
        totalSchdulCalList.addAll(schdulCalList);

        // 개인 일정 달력 목록 검색
        final boolean prvtChked = "Y".equals(searchParam.getPrvtChked());
        if (prvtChked) {
            final List<SchdulCalDto> prvtCalList = this.getPrvtCalList(searchParam);
            totalSchdulCalList.addAll(prvtCalList);
        }

        totalSchdulCalList.sort(Comparator.naturalOrder());

        return totalSchdulCalList;
    }

    /**
     * 일정 > 휴가 데이터 조회
     * 
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List<SchdulCalDto>} -- 휴가 일정 목록
     * @throws Exception 조회 및 처리 중 발생할 수 있는 예외
     */
    public List<SchdulCalDto> getVcatnCalList(final SchdulSearchParam searchParam) throws Exception {
        // 시작일, 종료일만 파라미터 추출
        final Map<String, Object> searchParamMap = new HashMap<>() {{
            put("searchStartDt", searchParam.getBgnDt());
            put("searchEndDt", searchParam.getEndDt());
        }};

        // 휴가 목록 검색
        final List<VcatnSchdulEntity> vcatnEntityList = vcatnSchdulService.getListEntity(searchParamMap);
        if (vcatnEntityList.isEmpty()) return Collections.emptyList();
        // entity -> calDto
        final List<SchdulCalDto> vcatnCalList = new ArrayList<>();
        for (VcatnSchdulEntity vcatn : vcatnEntityList) {
            // 각 휴가에 대해서 달력 일정 조회
            this.procVcatnCal(vcatn, vcatnCalList);
        }

        return vcatnCalList;
    }

    /**
     * 각 휴가에 대해서 휴가달력일정을 산정해서 목록에 추가
     *
     * @param vcatn 휴가 정보
     * @param vcatnCalList 산정 정보를 누적한 휴가달력일정
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    private void procVcatnCal(VcatnSchdulEntity vcatn, List<SchdulCalDto> vcatnCalList) throws Exception {
        final Date vcatnEndDt = DateUtils.Parser.sDateParse(vcatn.getEndDt());

        // 로직 :: 날짜 훑으면서 각 일자별로 쪼갬. 공휴일 또는 주말여부 체크
        Date keyDt = DateUtils.Parser.sDateParse(vcatn.getBgnDt());
        // 1. 첫날 객체부터 시작
        SchdulCalDto calDto = schdulCalMapstruct.toCalDto(vcatn);
        boolean isDelimStart = true;
        assert (keyDt != null && vcatnEndDt != null);
        // 2. 주말/공휴일 제외하고 출력하는 로직 :: 하루씩 순회하며 주말여부 체크
        while (keyDt != null && keyDt.compareTo(vcatnEndDt) <= 0) {
            Boolean isHldy = schdulService.isHldyOrWeekend(keyDt);
            boolean wasHldy = schdulService.isHldyOrWeekend(DateUtils.getDateAddDay(keyDt, -1));
            boolean changedToHldy = !wasHldy && isHldy;
            boolean isEndDt = DateUtils.isSameDay(keyDt, vcatnEndDt);

            if (!isHldy) {
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
                if (!isDelimStart && changedToHldy) {
                    this.finNewCalDto(calDto, DateUtils.getDateAddDay(keyDt, -1));
                    vcatnCalList.add(calDto);
                    isDelimStart = true;
                }
            }
            // 위 조건에 해당 안할시? 다음날짜로 넘어간다.
            keyDt = DateUtils.getDateAddDay(keyDt, 1);
        }
    }

    /**
     * 일정 정보에서 keyDt에 대해 CalDto 생성 :: 메소드 분리
     *
     * @param vcatn 일정 정보가 담긴 VcatnSchdulEntity 객체
     * @param keyDt 일정의 키 날짜
     * @return 생성된 SchdulCalDto 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    private SchdulCalDto initNewCalDto(final VcatnSchdulEntity vcatn, final Date keyDt) throws Exception {
        final SchdulCalDto calDto = schdulCalMapstruct.toCalDto(vcatn);
        calDto.setBgnDt(DateUtils.asStr(keyDt, DatePtn.DATE));
        if (StringUtils.isEmpty(calDto.getSchdulCd())) calDto.setSchdulCd(Constant.SCHDUL_VCATN);
        if (StringUtils.isEmpty(calDto.getClassName())) calDto.setClassName("cursor-pointer fc-event-danger fc-event-solid-warning");
        return calDto;
    }

    /**
     * 일정 정보에서 keyDt에 대해 CalDto 마무리 :: 메소드 분리
     *
     * @param calDto 일정 정보를 담고 있는 SchdulCalDto 객체
     * @param keyDt 일정의 키 날짜
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    private void finNewCalDto(final SchdulCalDto calDto, final Date keyDt) throws Exception {
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
     * @throws Exception 검색 중 발생할 수 있는 예외
     */
    public List<BaseCalDto> getHldyCalList(final BaseSearchParam searchParam) throws Exception {
        final Map<String, Object> searchParamMap = new HashMap<>() {{
            put("searchStartDt", searchParam.getSearchStartDt());
            put("searchEndDt", searchParam.getSearchEndDt());
            put("getHldyCeremonyOnly", true);
        }};

        // 일정 목록 검색
        final List<SchdulEntity> schdulEntityList = schdulRepository.findAll(schdulSpec.searchWith(searchParamMap));
        if (schdulEntityList.isEmpty()) return Collections.emptyList();

        // entity -> dto
        return schdulEntityList.stream()
                .map(entity -> {
                    try {
                        return schdulCalMapstruct.toCalDto(entity);
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
     * @throws Exception 검색 중 발생할 수 있는 예외
     */
    public List<SchdulCalDto> getSchdulCalList(final SchdulSearchParam searchParam) throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);
        final Map<String, Object> filteredSearchKey = CmmUtils.Param.filterParamMap(searchParamMap);
        // 일정 목록 검색
        final List<SchdulEntity> schdulEntityList = schdulRepository.findAll(schdulSpec.searchWith(filteredSearchKey));
        if (schdulEntityList.isEmpty()) return Collections.emptyList();

        return SchdulCalMapstruct.INSTANCE.toDtoList(schdulEntityList);
    }

    /**
     * 개인 일정 달력 목록 검색
     *
     * @param searchParam 일정 검색 파라미터
     * @return {@link List} -- 개인 일정 달력 목록 Dto 리스트
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    public List<SchdulCalDto> getPrvtCalList(final SchdulSearchParam searchParam) throws Exception {
        searchParam.setPrevOnly(true);
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);
        final Map<String, Object> filteredSearchKey = CmmUtils.Param.filterParamMap(searchParamMap);

        // 일정 목록 검색
        final List<SchdulEntity> schdulEntityList = schdulRepository.findAll(schdulSpec.searchWith(filteredSearchKey));
        if (schdulEntityList.isEmpty()) return Collections.emptyList();

        return SchdulCalMapstruct.INSTANCE.toDtoList(schdulEntityList);
    }

}
