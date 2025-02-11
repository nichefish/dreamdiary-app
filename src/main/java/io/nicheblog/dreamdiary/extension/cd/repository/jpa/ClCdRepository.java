package io.nicheblog.dreamdiary.extension.cd.repository.jpa;

import io.nicheblog.dreamdiary.extension.cd.entity.ClCdEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * ClCdRepository
 * <pre>
 *  분류 코드 repository 인터페이스.
 *  ※분류 코드(cl_cd) = 상위 분류 코드. 상세 코드(dtl_cd)를 1:N 묶음으로 관리한다.
 * </pre>
 *
 * @author nichefish
 */
@Repository("clCdRepository")
public interface ClCdRepository
        extends BaseStreamRepository<ClCdEntity, String> {
    //
}
