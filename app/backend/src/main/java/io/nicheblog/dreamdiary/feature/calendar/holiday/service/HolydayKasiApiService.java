package io.nicheblog.dreamdiary.feature.calendar.holiday.service;

import io.nicheblog.dreamdiary.feature.calendar.holiday.mapstruct.HolydayKasiApiMapstruct;
import io.nicheblog.dreamdiary.feature.calendar.holiday.model.HolydayKasiApiItemDto;
import io.nicheblog.dreamdiary.feature.calendar.holiday.model.HolydayKasiApiRespDto;
import io.nicheblog.dreamdiary.feature.calendar.schedule.entity.ScheduleEntity;
import io.nicheblog.dreamdiary.feature.calendar.schedule.service.ScheduleService;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HolydayKasiApiService
 * <pre>
 *  API:: 한국천문연구원(KASI):: 휴일 정보 조회 서비스 모듈
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class HolydayKasiApiService {

    private final HolydayKasiApiMapstruct holydayApiMapstruct = HolydayKasiApiMapstruct.INSTANCE;
    private final ScheduleService scheduleService;

    private final ApplicationContext context;
    private HolydayKasiApiService getSelf() {
        return context.getBean(this.getClass());
    }

    @Value("${app.integration.kasi.service-key}")
    private String serviceKey;
    @Value("${app.integration.kasi.api-url}")
    private String serviceUrl;

    /**
     * API로 받아온 휴일 정보를 DB에서 삭제 후 재등록
     * <pre>
         * 1. 기존 데이터 삭제
         * 2. 새로운 휴일 정보를 API에서 조회
         * 3. 조회된 데이터를 DB에 저장
     * </pre>
     *
     * @param yyStr 조회 및 처리할 연도 (String) - 없으면 현재 연도를 사용
     * @return {@link Boolean} -- 성공적으로 처리된 경우 true
     */
    @Transactional
    public Boolean procHolydayList(final String yyStr) throws Exception {
        // 기존 정보 (API로 받아온 휴일) 삭제 후 재등록
        this.delHolydayList(yyStr);
        List<HolydayKasiApiItemDto> holydayApiList = this.getHolydayList(yyStr);
        return this.regHolydayList(holydayApiList);
    }

    /**
     * API:: 한국천문연구원(KASI):: 휴일 정보 조회
     *
     * @param yyParam 조회할 연도 (String), 비어 있을 경우 현재 연도로 설정
     * @return {@link List} -- 휴일 정보 리스트
     */
    public List<HolydayKasiApiItemDto> getHolydayList(final String yyParam) throws Exception {
        String yyStr = !StringUtils.isEmpty(yyParam) ? yyParam : DateUtils.getCurrYyStr();
        RestTemplate restTemplate = new RestTemplate();
        List<HolydayKasiApiItemDto> rsItems = new ArrayList<>();
        try {
            // URL 설정
            int numOfRows = 30;
            URI requestURI = new URI(serviceUrl + "?solYear=" + yyStr + "&numOfRows=" + numOfRows + "&ServiceKey=" + serviceKey);
            // 요청 생성
            HolydayKasiApiRespDto respDto = restTemplate.getForObject(requestURI, HolydayKasiApiRespDto.class);
            if (respDto != null) rsItems = respDto.getBody().getItems();
        } catch (final Exception e) {
            MessageUtils.getExceptionMsg(e);
        }
        return rsItems;
    }

    /**
     * API:: 한국천문연구원(KASI):: 휴일 정보 받아와서 DB 저장
     *
     * @param holydayApiList 휴일 정보 리스트
     * @return {@link Boolean} -- 성공적으로 저장된 경우 true
     */
    @Transactional
    @CacheEvict(value = {"holydayEntityList", "isHolyday", "isHolydayOrWeekend"}, allEntries = true)
    public Boolean regHolydayList(final List<HolydayKasiApiItemDto> holydayApiList) throws Exception {
        if (CollectionUtils.isEmpty(holydayApiList)) return true;
        // dto to entity
        List<ScheduleEntity> scheduleList = holydayApiList.stream()
                .map(holyday -> {
                    try {
                        return holydayApiMapstruct.toEntity(holyday);
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
        scheduleService.registAll(scheduleList);

        return true;
    }

    /**
     * API:: 한국천문연구원(KASI):: API 조회 휴일 정보 DB 삭제
     *
     * @param yyStr 삭제할 연도 (String)
     */
    @Transactional
    @CacheEvict(value = {"holydayEntityList", "isHolyday", "isHolydayOrWeekend"}, allEntries = true)
    public void delHolydayList(final String yyStr) throws Exception {
        final Map<String, Object> searchParamMap = new HashMap<>() {{
            put("searchStartDt", DateUtils.asDate(yyStr + "-01-01"));
            put("searchEndDt", DateUtils.Parser.eDateParse(DateUtils.asDate(yyStr + "-12-31")));
            put("src", "KASI");
        }};
        scheduleService.deleteAll(searchParamMap);
    }

    /**
     * API:: 한국천문연구원(KASI):: API 조회 휴일 정보 DB 삭제 및 재등록
     *
     * @param yyStr 삭제할 연도 (String)
     */
    @Transactional
    public boolean resyncHolyday(final String yyStr) throws Exception {
        this.delHolydayList(yyStr);

        final List<HolydayKasiApiItemDto> holydayApiList = this.getSelf().getHolydayList(yyStr);
        final boolean isSuccess = this.getSelf().regHolydayList(holydayApiList);

        if (isSuccess) scheduleService.resyncHolydayMap();

        return isSuccess;
    }
}
