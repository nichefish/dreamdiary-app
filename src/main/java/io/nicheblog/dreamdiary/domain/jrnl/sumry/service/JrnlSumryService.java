package io.nicheblog.dreamdiary.domain.jrnl.sumry.service;

import io.nicheblog.dreamdiary.domain.jrnl.sumry.entity.JrnlSumryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.mapstruct.JrnlSumryMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.model.JrnlSumryDto;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.repository.jpa.JrnlSumryRepository;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.spec.JrnlSumrySpec;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseMultiCrudService;

import java.util.List;

/**
 * JrnlSumryService
 * <pre>
 *  저널 결산 관리 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface JrnlSumryService
        extends BaseMultiCrudService<JrnlSumryDto.DTL, JrnlSumryDto.LIST, Integer, JrnlSumryEntity, JrnlSumryRepository, JrnlSumrySpec, JrnlSumryMapstruct> {

    /**
     * 저널 결산 내 정뵤 목록 조회
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link List<JrnlSumryDto.LIST>} -- 검색 조건에 맞는 결산 목록 Dto 리스트
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    List<JrnlSumryDto.LIST> getMyListDto(final BaseSearchParam searchParam) throws Exception;

    /**
     * 년도를 받아서 해당 년도 저널 결산 정보 생성
     *
     * @return {@link Boolean} -- 결산 생성 성공 여부 (항상 true 반환)
     * @throws Exception 결산 생성 중 발생할 수 있는 예외
     */
    Boolean makeYySumry(final Integer yy) throws Exception;

    /**
     * 2011년부터 현재 년도까지의 저널 결산 정보 생성
     *
     * @return {@link Boolean} -- 결산 생성 성공 여부 (항상 true 반환)
     * @throws Exception 결산 생성 중 발생할 수 있는 예외
     */
    Boolean makeTotalYySumry() throws Exception;

    /**
     * 관련 정보를 취합하여 총 저널 결산 정보를 생성합니다. (캐시 처리)
     * 
     * @return {@link JrnlSumryDto} -- 총 결산 정보가 담긴 Dto 객체
     */
    JrnlSumryDto getTotalSumry();

    /**
     * 저널 결산 상세 정보 조회 (캐시 처리)
     *
     * @param key 식별자
     * @return {@link JrnlSumryDto.DTL} -- 조회된 결산 정보가 담긴 Dto 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    JrnlSumryDto.DTL getSumryDtl(final Integer key) throws Exception;

    /**
     * 년도별 저널 결산 정보 조회 (캐시 처리)
     *
     * @param yy 조회할 년도
     * @return {@link JrnlSumryDto} -- 조회된 결산 정보가 담긴 Dto 객체, 없을 경우 null 반환
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    JrnlSumryDto getDtlDtoByYy(final Integer yy) throws Exception;

    /**
     * 저널 결산 꿈 기록 완료 처리
     *
     * @param key 식별자
     * @return {@link boolean} -- 처리 성공 여부
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    boolean dreamCompt(final Integer key) throws Exception;
}