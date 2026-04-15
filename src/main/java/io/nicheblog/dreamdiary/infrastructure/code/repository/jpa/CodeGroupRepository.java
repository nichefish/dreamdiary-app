package io.nicheblog.dreamdiary.infrastructure.code.repository.jpa;

import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeGroupEntity;
import org.springframework.stereotype.Repository;

/**
 * CodeGroupRepository
 * <pre>
 *  분류 코드 repository 인터페이스.
 *  ※분류 코드(cl_cd) = 상위 분류 코드. 상세 코드(dtl_cd)를 1:N 묶음으로 관리한다.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface CodeGroupRepository
        extends BaseStreamRepository<CodeGroupEntity, String> {
    //
}
