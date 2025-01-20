package io.nicheblog.dreamdiary.adapter.kasi.service.impl;

import io.nicheblog.dreamdiary.adapter.kasi.mapstruct.HldyKasiApiMapstruct;
import io.nicheblog.dreamdiary.adapter.kasi.model.HldyKasiApiItemDto;
import io.nicheblog.dreamdiary.adapter.kasi.model.HldyKasiApiRespDto;
import io.nicheblog.dreamdiary.adapter.kasi.service.HldyKasiApiService;
import io.nicheblog.dreamdiary.domain.schdul.entity.SchdulEntity;
import io.nicheblog.dreamdiary.domain.schdul.service.SchdulService;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HldyKasiApiService
 * <pre>
 *  API:: 한국천문연구원(KASI):: 휴일 정보 조회 서비스 모듈
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class HldyKasiApiServiceImpl
        implements HldyKasiApiService {

    private final HldyKasiApiMapstruct hldyApiMapstruct = HldyKasiApiMapstruct.INSTANCE;
    private final SchdulService schdulService;

    @Value("${api.kasi.serviceKey}")
    private String serviceKey;
    @Value("${api.kasi.api-url}")
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
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    @Transactional
    public Boolean procHldyList(final String yyStr) throws Exception {
        // 기존 정보 (API로 받아온 휴일) 삭제 후 재등록
        this.delHldyList(yyStr);
        List<HldyKasiApiItemDto> hldyApiList = this.getHldyList(yyStr);
        return this.regHldyList(hldyApiList);
    }

    /**
     * API:: 한국천문연구원(KASI):: 휴일 정보 조회
     *
     * @param yyParam 조회할 연도 (String), 비어 있을 경우 현재 연도로 설정
     * @return {@link List} -- 휴일 정보 리스트
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    @Override
    public List<HldyKasiApiItemDto> getHldyList(final String yyParam) throws Exception {
        String yyStr = !StringUtils.isEmpty(yyParam) ? yyParam : DateUtils.getCurrYyStr();
        RestTemplate restTemplate = new RestTemplate();
        List<HldyKasiApiItemDto> rsItems = new ArrayList<>();
        try {
            // URL 설정
            int numOfRows = 30;
            URI requestURI = new URI(serviceUrl + "?solYear=" + yyStr + "&numOfRows=" + numOfRows + "&ServiceKey=" + serviceKey);
            // 요청 생성
            HldyKasiApiRespDto respDto = restTemplate.getForObject(requestURI, HldyKasiApiRespDto.class);
            if (respDto != null) rsItems = respDto.getBody().getItems();
        } catch (final Exception e) {
            MessageUtils.getExceptionMsg(e);
        }
        return rsItems;
    }

    /**
     * API:: 한국천문연구원(KASI):: 휴일 정보 받아와서 DB 저장
     *
     * @param hldyApiList 휴일 정보 리스트
     * @return {@link Boolean} -- 성공적으로 저장된 경우 true
     * @throws Exception 저장 중 발생할 수 있는 예외
     */
    @Override
    @Transactional
    @CacheEvict(value = {"hldyEntityList", "isHldy", "isHldyOrWeekend"}, allEntries = true)
    public Boolean regHldyList(final List<HldyKasiApiItemDto> hldyApiList) throws Exception {
        if (CollectionUtils.isEmpty(hldyApiList)) return true;
        // dto to entity
        List<SchdulEntity> schdulList = hldyApiList.stream()
                .map(hldy -> {
                    try {
                        return hldyApiMapstruct.toEntity(hldy);
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
        schdulService.registAll(schdulList);

        return true;
    }

    /**
     * API:: 한국천문연구원(KASI):: API 조회 휴일 정보 DB 삭제
     *
     * @param yyStr 삭제할 연도 (String)
     * @throws Exception 삭제 중 발생할 수 있는 예외
     */
    @Override
    @Transactional
    @CacheEvict(value = {"hldyEntityList", "isHldy", "isHldyOrWeekend"}, allEntries = true)
    public void delHldyList(final String yyStr) throws Exception {
        final Map<String, Object> searchParamMap = new HashMap<>() {{
            put("searchStartDt", DateUtils.asDate(yyStr + "-01-01"));
            put("searchEndDt", DateUtils.Parser.eDateParse(DateUtils.asDate(yyStr + "-12-31")));
            put("src", "KASI");
        }};
        schdulService.deleteAll(searchParamMap);
    }
}
