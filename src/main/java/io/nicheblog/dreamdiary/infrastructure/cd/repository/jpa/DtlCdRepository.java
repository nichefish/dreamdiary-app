package io.nicheblog.dreamdiary.infrastructure.cd.repository.jpa;

import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import io.nicheblog.dreamdiary.infrastructure.cd.entity.DtlCdEntity;
import io.nicheblog.dreamdiary.infrastructure.cd.entity.DtlCdKey;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * DtlCdRepository
 * <pre>
 *  상세 코드 repository 인터페이스
 *  ※상세 코드(dtl_cd) = 분류 코드 하위의 상세 코드. 분류 코드(cl_cd)에 N:1로 귀속된다.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface DtlCdRepository
        extends BaseStreamRepository<DtlCdEntity, DtlCdKey> {

    /**
     * 분류 코드로 상세 코드 목록 검색.
     *
     * @param clCd 검색할 분류 코드
     * @return {@link Boolean} -- 분류 코드에 해당하는 상세 코드 목록
     */
    List<DtlCdEntity> findByClCd(final String clCd);

    /**
     * 분류코드 기준 상세코드 목록을 정렬 순서 기준으로 조회.
     *
     * @param clCd 분류코드.
     * @return 상세코드 목록.
     */
    List<DtlCdEntity> findByClCdOrderByIdxAscDtlCdAsc(final String clCd);

    /**
     * 분류코드 기준으로 사용중인 상세코드 목록을 조회.
     *
     * @param clCd 분류코드.
     * @param useYn 사용여부.
     * @return 상세코드 목록.
     */
    List<DtlCdEntity> findByClCdAndUseYnOrderByIdxAsc(final String clCd, final String useYn);

    /**
     * 사용중인 상세코드를 메모리 preload 용도로 조회.
     *
     * @param useYn 사용여부.
     * @return 정렬된 상세코드 목록.
     */
    List<DtlCdEntity> findAllByUseYnOrderByClCdAscIdxAsc(final String useYn);

    /**
     * 공통코드, 상세 코드로 상세 코드명 조회.
     *
     * @param clCd 공통 코드
     * @param dtlCd 상세 코드
     * @return {@link DtlCdEntity} -- 상세 코드
     */
    DtlCdEntity findByClCdAndDtlCd(final String clCd, final String dtlCd);
}

