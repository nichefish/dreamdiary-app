package io.nicheblog.dreamdiary.feature.clsf.tag.repository.jpa;

import io.nicheblog.dreamdiary.feature.clsf.tag.entity.TagCategoryProfileEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository("tagCategoryProfileRepository")
public interface TagCategoryProfileRepository
        extends BaseStreamRepository<TagCategoryProfileEntity, Integer> {

    Optional<TagCategoryProfileEntity> findByTagCategoryIdAndContentTypeAndCreatedBy(
            final Integer tagCategoryId,
            final String contentType,
            final String createdBy
    );

    List<TagCategoryProfileEntity> findAllByTagCategoryIdInAndContentTypeAndCreatedBy(
            final Collection<Integer> tagCategoryIdList,
            final String contentType,
            final String createdBy
    );
}
