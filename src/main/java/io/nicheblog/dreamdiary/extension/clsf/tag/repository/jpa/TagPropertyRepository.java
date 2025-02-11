package io.nicheblog.dreamdiary.extension.clsf.tag.repository.jpa;

import io.nicheblog.dreamdiary.extension.clsf.tag.entity.TagPropertyEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * TagPropertyRepository
 * <pre>
 *  태그 속성 정보 repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("tagPropertyRepository")
public interface TagPropertyRepository
        extends BaseStreamRepository<TagPropertyEntity, Integer> {
    //
}
