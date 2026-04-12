package io.nicheblog.dreamdiary.feature.clsf.tag.repository.jpa;

import io.nicheblog.dreamdiary.feature.clsf.tag.entity.TagProfileEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * TagProfileRepository
 * <pre>
 *  태그 프로필(해석) repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("tagProfileRepository")
public interface TagProfileRepository
        extends BaseStreamRepository<TagProfileEntity, Integer> {

    /**
     * 태그 ID + 컨텐츠 타입으로 프로필 단건 조회.
     *
     * @param tagId 태그 ID
     * @param contentType 컨텐츠 타입
     * @return {@link Optional} -- 조회된 TagProfileEntity
     */
    Optional<TagProfileEntity> findByTagIdAndContentTypeAndRegstrId(final Integer tagId, final String contentType, final String regstrId);

    List<TagProfileEntity> findAllByTagIdInAndContentTypeAndRegstrId(final Collection<Integer> tagIdList, final String contentType, final String regstrId);

    Optional<TagProfileEntity> findByIdAndRegstrId(final Integer id, final String regstrId);
}
