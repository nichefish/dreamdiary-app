package io.nicheblog.dreamdiary.domain.vcatn.papr.service.impl;

import io.nicheblog.dreamdiary.domain.vcatn.papr.entity.VcatnPaprEntity;
import io.nicheblog.dreamdiary.domain.vcatn.papr.entity.VcatnSchdulEntity;
import io.nicheblog.dreamdiary.domain.vcatn.papr.mapstruct.VcatnPaprMapstruct;
import io.nicheblog.dreamdiary.domain.vcatn.papr.model.VcatnPaprDto;
import io.nicheblog.dreamdiary.domain.vcatn.papr.model.VcatnSchdulDto;
import io.nicheblog.dreamdiary.domain.vcatn.papr.repository.jpa.VcatnPaprRepository;
import io.nicheblog.dreamdiary.domain.vcatn.papr.service.VcatnPaprService;
import io.nicheblog.dreamdiary.domain.vcatn.papr.service.VcatnSchdulService;
import io.nicheblog.dreamdiary.domain.vcatn.papr.spec.VcatnPaprSpec;
import io.nicheblog.dreamdiary.domain.vcatn.stats.model.VcatnStatsYyDto;
import io.nicheblog.dreamdiary.domain.vcatn.stats.service.VcatnStatsYyService;
import io.nicheblog.dreamdiary.extension.cd.service.DtlCdService;
import io.nicheblog.dreamdiary.extension.clsf.viewer.event.ViewerAddEvent;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * VcatnPaprService
 * <pre>
 *  휴가계획서 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("vcatnPaprService")
@RequiredArgsConstructor
@Log4j2
public class VcatnPaprServiceImpl
        implements VcatnPaprService {

    @Getter
    private final VcatnPaprRepository repository;
    @Getter
    private final VcatnPaprSpec spec;
    @Getter
    private final VcatnPaprMapstruct mapstruct = VcatnPaprMapstruct.INSTANCE;

    private final VcatnSchdulService vcatnSchdulService;
    private final VcatnStatsYyService vcatnStatsYyService;
    private final DtlCdService dtlCdService;
    private final ApplicationEventPublisherWrapper publisher;

    /**
     * 휴가계획서 중 가장 오래된 등록 년도부터 현재 년도까지의 년도 목록을 생성하여 반환합니다.
     *
     * @return {@link List} -- 휴가계획서 년도 목록
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    public List<VcatnStatsYyDto> getVcatnYyList() throws Exception {
        final String minYyStr = repository.selectMinYy();
        final int minYy = (minYyStr != null) ? Integer.parseInt(minYyStr) : DateUtils.getCurrYy();
        final List<VcatnStatsYyDto> yyList = new ArrayList<>();
        final int currYy = DateUtils.getCurrYy();
        for (int yy = minYy; yy <= currYy; yy++) {     // 최저년도~현재년도. 현재년도는 항상 들어가도록
            yyList.add(vcatnStatsYyService.getVcatnYyDtDto(Integer.toString(yy)));
        }

        return yyList;
    }

    /**
     * 등록 전처리. (override)
     * 
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final VcatnPaprDto.DTL registDto) {
        // 제목 자동 처리
        registDto.setTitle(this.initTitle(registDto));
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postRegist(final VcatnPaprDto.DTL updatedDto) throws Exception {
        // 조치자 추가 :: 메인 로직과 분리
        publisher.publishAsyncEvent(new ViewerAddEvent(this, updatedDto.getClsfKey()));
    }

    /**
     * 상세 페이지 조회 후처리 (dto level)
     *
     * @param retrievedDto - 조회된 Dto 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postViewDtlPage(final VcatnPaprDto.DTL retrievedDto) throws Exception {
        // 열람자 추가 :: 메인 로직과 분리
        publisher.publishAsyncEvent(new ViewerAddEvent(this, retrievedDto.getClsfKey()));
    }

    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto 수정할 객체
     */
    @Override
    public void preModify(final VcatnPaprDto.DTL modifyDto) {
        // 제목 자동 처리
        modifyDto.setTitle(this.initTitle(modifyDto));
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postModify(final VcatnPaprDto.DTL updatedDto) throws Exception {
        // 조치자 추가 :: 메인 로직과 분리
        publisher.publishAsyncEvent(new ViewerAddEvent(this, updatedDto.getClsfKey()));
    }

    /**
     * 제목 자동 처리.
     * 
     * @param vcatnPaprDto 처리할 객체
     */
    @Override
    public String initTitle(final VcatnPaprDto vcatnPaprDto) {
        final List<VcatnSchdulDto> schdulList = vcatnPaprDto.getSchdulList();
        if (CollectionUtils.isEmpty(schdulList)) return "휴가계획서 (날짜없음)";

        return schdulList.stream()
                .map(vcatn -> {
                    final String vcatnCd = vcatn.getVcatnCd();
                    final String vcatnNm = dtlCdService.getDtlCdNm(Constant.VCATN_CD, vcatnCd);
                    final String endDt = vcatn.getEndDt();
                    String period = vcatn.getBgnDt();
                    try {
                        final boolean isSameDay = DateUtils.isSameDay(period, endDt) || (endDt == null);
                        if (!isSameDay) {
                            period += "~" + endDt;
                        }
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }

                    return "(" + vcatnNm + ")" + period;
                })
                .collect(Collectors.joining(", "));
    }

    /**
     * 일정 > 휴가계획서 > 휴가계획서 상세보기 > 확인 여부 변경
     */
    @Override
    @Transactional
    public ServiceResponse cf(final Integer key) throws Exception {
        final VcatnPaprEntity vcatnPaprEntity = this.getDtlEntity(key);
        vcatnPaprEntity.setCfYn("Y");
        // update
        final VcatnPaprEntity updatedEntity = this.updt(vcatnPaprEntity);
        final VcatnPaprDto updatedDto = mapstruct.toDto(updatedEntity);

        return ServiceResponse.builder()
                .rslt(updatedEntity.getPostNo() != null)
                .rsltObj(updatedDto)
                .build();
    }

    /**
     * 삭제 전처리. (override)
     *
     * @param deleteEntity - 삭제할 엔티티
     */
    @Override
    public void preDelete(final VcatnPaprEntity deleteEntity) throws Exception {
        // 휴가계획서 상세항목 삭제
        List<VcatnSchdulEntity> vcatnSchdulList = deleteEntity.getSchdulList();
        vcatnSchdulService.deleteAll(vcatnSchdulList);
    }
}
