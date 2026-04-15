package io.nicheblog.dreamdiary.feature.clsf.tag.repository.jpa;

import io.nicheblog.dreamdiary.feature.clsf.tag.entity.TagSmpEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * TagSmpRepository
 * <pre>
 *  태그 간소화 정보 repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface TagSmpRepository
        extends BaseStreamRepository<TagSmpEntity, Integer> {
    //
}

