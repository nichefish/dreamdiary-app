package io.nicheblog.dreamdiary.global._common.cd.service;

import io.nicheblog.dreamdiary.extension.state.service.BaseStateService;
import io.nicheblog.dreamdiary.global._common.cd.entity.DtlCdEntity;
import io.nicheblog.dreamdiary.global._common.cd.entity.DtlCdKey;
import io.nicheblog.dreamdiary.global._common.cd.mapstruct.DtlCdMapstruct;
import io.nicheblog.dreamdiary.global._common.cd.model.DtlCdDto;
import io.nicheblog.dreamdiary.global._common.cd.repository.jpa.DtlCdRepository;
import io.nicheblog.dreamdiary.global._common.cd.spec.DtlCdSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseCrudService;
import org.springframework.ui.ModelMap;

import java.util.List;

/**
 * DtlCdService
 * <pre>
 *  상세 코드 관리 서비스 모듈
 *  ※상세 코드(dtl_cd) = 분류 코드 하위의 상세 코드. 분류 코드(cl_cd)에 N:1로 귀속된다.
 * </pre>
 *
 * @author nichefish
 */
public interface DtlCdService
        extends BaseCrudService<DtlCdDto, DtlCdDto, DtlCdKey, DtlCdEntity, DtlCdRepository, DtlCdSpec, DtlCdMapstruct>,
                BaseStateService<DtlCdDto, DtlCdDto, DtlCdKey, DtlCdEntity, DtlCdRepository, DtlCdSpec, DtlCdMapstruct> {

    /**
     * 공통 - 코드 정보를 Model에 추가합니다.
     *
     * @param clCd 분류 코드
     * @param model ModelMap 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    void setCdListToModel(final String clCd, final ModelMap model) throws Exception;

    /**
     * 분류 코드로 상세 코드 목록 조회 (entity level)
     *
     * @param clCd 분류 코드
     * @return {@link List} -- 상세 코드 목록 (entity level)
     */
    List<DtlCdEntity> getCdEntityListByClCd(final String clCd) throws Exception;

    /**
     * 분류 코드로 상세 코드 목록 조회 (dto level)
     *
     * @param clCd 분류 코드
     * @return {@link List} -- 상세 코드 목록 (dto level)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    List<DtlCdDto> getCdDtoListByClCd(final String clCd) throws Exception;

    /**
     * 분류 코드, 상세 코드로 상세 코드명 조회
     *
     * @param clCd 분류 코드 (String)
     * @param dtlCd 상세 코드 (String)
     * @return {@link String} -- 상세 코드명
     */
    String getDtlCdNm(final String clCd, final String dtlCd);
}