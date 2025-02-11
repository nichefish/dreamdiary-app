package io.nicheblog.dreamdiary.extension.cd.repository.jpa;

import io.nicheblog.dreamdiary.extension.cd.entity.DtlCdEntity;
import io.nicheblog.dreamdiary.extension.cd.entity.DtlCdKey;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
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
@Repository("dtlCdRepository")
public interface DtlCdRepository
        extends BaseStreamRepository<DtlCdEntity, DtlCdKey> {

    /**
     * 분류 코드로 상세 코드 목록 검색.
     *
     * @param clCd 검색할 분류 코드
     * @return {@link Boolean} -- 분류 코드에 해당하는 상세 코드 목록 (List<DtlCdEntity>)
     */
    List<DtlCdEntity> findByClCd(final String clCd);

    /**
     * 분류 코드로 '사용 중'인 상세 코드 목록 검색.
     *
     * @param clCd 분류 코드
     * @param useYn 사용 여부 (Y/N)
     * @param sort 정렬 기준
     * @return {@link List} -- 상세 코드 목록
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<DtlCdEntity> findByClCdAndStateUseYn(final String clCd, String useYn, final Sort sort);

    /**
     * 공통코드, 상세 코드로 상세 코드명 조회.
     *
     * @param clCd 공통 코드
     * @param dtlCd 상세 코드
     * @return {@link DtlCdEntity} -- 상세 코드
     */
    DtlCdEntity findByClCdAndDtlCd(final String clCd, final String dtlCd);
}
