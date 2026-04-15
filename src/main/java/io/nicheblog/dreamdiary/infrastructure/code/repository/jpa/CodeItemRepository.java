package io.nicheblog.dreamdiary.infrastructure.code.repository.jpa;

import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeItemEntity;
import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeItemKey;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CodeItemRepository
 * <pre>
 *  상세 코드 repository 인터페이스
 *  ※상세 코드(dtl_cd) = 분류 코드 하위의 상세 코드. 분류 코드(cl_cd)에 N:1로 귀속된다.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface CodeItemRepository
        extends BaseStreamRepository<CodeItemEntity, CodeItemKey> {

    List<CodeItemEntity> findByClCd(final String clCd);

    List<CodeItemEntity> findByClCdOrderBySortOrderAscDtlCdAsc(final String clCd);

    List<CodeItemEntity> findByClCdAndUseYnOrderBySortOrderAsc(final String clCd, final String useYn);

    List<CodeItemEntity> findAllByUseYnOrderByClCdAscSortOrderAsc(final String useYn);

    CodeItemEntity findByClCdAndDtlCd(final String clCd, final String dtlCd);
}
