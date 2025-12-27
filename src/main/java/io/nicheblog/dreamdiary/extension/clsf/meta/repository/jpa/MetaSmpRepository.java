package io.nicheblog.dreamdiary.extension.clsf.meta.repository.jpa;

import io.nicheblog.dreamdiary.extension.clsf.meta.entity.MetaSmpEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * MetaSmpRepository
 * <pre>
 *  메타 간소화 정보 repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("metaSmpRepository")
public interface MetaSmpRepository
        extends BaseStreamRepository<MetaSmpEntity, Integer> {
    //
}
