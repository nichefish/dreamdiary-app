package io.nicheblog.dreamdiary.domain.vcatn.papr.service;

import io.nicheblog.dreamdiary.domain.vcatn.papr.entity.VcatnPaprEntity;
import io.nicheblog.dreamdiary.domain.vcatn.papr.mapstruct.VcatnPaprMapstruct;
import io.nicheblog.dreamdiary.domain.vcatn.papr.model.VcatnPaprDto;
import io.nicheblog.dreamdiary.domain.vcatn.papr.repository.jpa.VcatnPaprRepository;
import io.nicheblog.dreamdiary.domain.vcatn.papr.spec.VcatnPaprSpec;
import io.nicheblog.dreamdiary.domain.vcatn.stats.model.VcatnStatsYyDto;
import io.nicheblog.dreamdiary.global.intrfc.service.BasePostService;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;

import java.util.List;

/**
 * VcatnPaprService
 * <pre>
 *  휴가계획서 서비스 인터페이스 모듈.
 * </pre>
 *
 * @author nichefish
 */
public interface VcatnPaprService
        extends BasePostService<VcatnPaprDto.DTL, VcatnPaprDto.LIST, Integer, VcatnPaprEntity, VcatnPaprRepository, VcatnPaprSpec, VcatnPaprMapstruct> {
    
    /**
     * 휴가계획서 중 가장 오래된 등록 년도부터 현재 년도까지의 년도 목록을 생성하여 반환합니다.
     *
     * @return {@link List} -- 휴가계획서 년도 목록ㅁ
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    List<VcatnStatsYyDto> getVcatnYyList() throws Exception;
    
    /**
     * 제목 자동 처리.
     * 
     * @param vcatnPaprDto 처리할 객체
     */
    String initTitle(final VcatnPaprDto vcatnPaprDto);

    /**
     * 일정 > 휴가계획서 > 휴가계획서 상세보기 > 확인 여부 변경
     */
    ServiceResponse cf(final Integer key) throws Exception;
}
