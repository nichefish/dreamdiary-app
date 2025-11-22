package io.nicheblog.dreamdiary.domain.vcatn.papr.service;

import io.nicheblog.dreamdiary.domain.vcatn.papr.entity.VcatnSchdulEntity;
import io.nicheblog.dreamdiary.domain.vcatn.papr.mapstruct.VcatnSchdulMapstruct;
import io.nicheblog.dreamdiary.domain.vcatn.papr.model.VcatnSchdulDto;
import io.nicheblog.dreamdiary.domain.vcatn.papr.repository.jpa.VcatnSchdulRepository;
import io.nicheblog.dreamdiary.domain.vcatn.papr.spec.VcatnSchdulSpec;
import io.nicheblog.dreamdiary.domain.vcatn.stats.model.VcatnStatsYyDto;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseCrudService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * VcatnPaprService
 * <pre>
 *  휴가계획서 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("vcatnSchdulService")
@RequiredArgsConstructor
@Log4j2
public class VcatnSchdulService
        implements BaseCrudService<VcatnSchdulDto, VcatnSchdulDto, Integer, VcatnSchdulEntity, VcatnSchdulRepository, VcatnSchdulSpec, VcatnSchdulMapstruct> {

    @Getter
    private final VcatnSchdulRepository repository;
    @Getter
    private final VcatnSchdulSpec spec;
    @Getter
    private final VcatnSchdulMapstruct mapstruct = VcatnSchdulMapstruct.INSTANCE;

    /**
     * 휴가 년도 정보를 바탕으로 휴가 일정 목록을 조회합니다.
     *
     * @param statsYy 휴가 년도 정보를 담고 있는 객체
     * @return {@link List} 휴가 일정 목록
     */
    public List<VcatnSchdulDto> getListDto(VcatnStatsYyDto statsYy) throws Exception {
        final Map<String, Object> searchParamMap = new HashMap(){{
            put("statsYy", statsYy.getStatsYy());
            put("statsYy", statsYy.getStatsYy());
        }};
        return this.getListDto(searchParamMap);
    }

/**
 * 목록 Page<Entity> -> Page<Dto> 변환 (override)
 *
 * @param entityPage 페이징 처리된 Entity 목록
 * @return {@link Page} -- 변환된 페이징 처리된 Dto 목록
 *//*

    public Page<VcatnSchdulDto> pageEntityToDto(
            final Page<VcatnSchdulEntity> entityPage,
            final Map<String, Object> searchParamMap
    ) throws Exception {
        Date statsYyBgnDt = DateUtils.asDate(searchParamMap.get("searchStartDt"));
        Date statsYyEndDt = DateUtils.asDate(searchParamMap.get("searchEndDt"));
        List<VcatnSchdulDto> rsDyDtoList = new ArrayList<>();
        for (VcatnSchdulEntity vcatn : entityPage.getContent()) {
            // 연차(반차)만 계산한다. 공가, 무급/생리휴가, 경조휴가 등은 패스
            String vcatnTy = vcatn.getVcatnCd();
            // 반차는 0.5일, 연차는 1일 소진
            boolean isHalf = (Constant.VCATN_AM_HALF.equals(vcatnTy) || Constant.VCATN_PM_HALF.equals(vcatnTy));
            double exhrDy = isHalf ? 0.5 : Constant.VCATN_ANNUAL.equals(vcatnTy) ? 1 : 0;

            // 휴가 시작일자/종료일자 산정 : 2월 28일 전- 또는 3월 1일 후 식으로 경계에 걸쳐있는 경우를 따진다. (딱 경계까지만 일수 계산하기)
            Date vcatnBgnDt = vcatn.getBgnDt();
            Date vcatnEndDt = vcatn.getEndDt();
            if (vcatnBgnDt.compareTo(statsYyBgnDt) < 0) vcatnBgnDt = statsYyBgnDt;
            vcatnEndDt = (vcatnEndDt == null) ? vcatnBgnDt : (vcatnEndDt.compareTo(statsYyEndDt) > 0) ? statsYyEndDt : vcatnEndDt;

            // 날짜 훑으면서 각 일자별로 쪼갬. 공휴일 또는 주말여부 체크
            Date keyDt = vcatn.getBgnDt();
            while (keyDt.compareTo(vcatnEndDt) <= 0) {
                if (schdulService.isHldyOrWeekend(keyDt)) {
                    keyDt = DateUtils.getDateAddDay(keyDt, 1);
                    continue;
                }
                VcatnSchdulDto vcatnSchdul = vcatnSchdulMapstruct.toDyDto(vcatn);
                // 첫째날에는 특별히 수정 버튼 등 이것저것 달아줌
                if (keyDt.compareTo(vcatnBgnDt) == 0) {
                    vcatnSchdul.setVcatnSchdulNo(Integer.toString(vcatn.getVcatnSchdulNo()));
                    vcatnSchdul.setRm(vcatn.getRm());
                }
                vcatnSchdul.setVcatnSchdul(DateUtils.asStr(keyDt, DatePtn.DATE));
                vcatnSchdul.setVcatnExprDy(exhrDy);
                rsDyDtoList.add(vcatnSchdul);
                keyDt = DateUtils.getDateAddDay(keyDt, 1);
            }
        }
        rsDyDtoList.sort(Comparator.naturalOrder());
        return new PageImpl<>(rsDyDtoList, entityPage.getPageable(), entityPage.getTotalElements());
    }

}*/

}
