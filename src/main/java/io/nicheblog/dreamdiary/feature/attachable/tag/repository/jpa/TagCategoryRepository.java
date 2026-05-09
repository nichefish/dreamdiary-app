package io.nicheblog.dreamdiary.feature.attachable.tag.repository.jpa;

import io.nicheblog.dreamdiary.feature.attachable.tag.entity.TagCategoryEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * TagCategoryRepository
 * <pre>
 *  태그 카테고리 repository interface.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface TagCategoryRepository
        extends BaseStreamRepository<TagCategoryEntity, Integer> {

    Optional<TagCategoryEntity> findByName(String name);
}

