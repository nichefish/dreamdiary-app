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
     * 태그 번호 + 컨텐츠 타입으로 프로필 단건 조회.
     *
     * @param tagNo 태그 번호
     * @param contentType 컨텐츠 타입
     * @return {@link Optional} -- 조회된 TagProfileEntity
     */
    Optional<TagProfileEntity> findByTagNoAndContentTypeAndRegstrId(final Integer tagNo, final String contentType, final String regstrId);

    List<TagProfileEntity> findAllByTagNoInAndContentTypeAndRegstrId(final Collection<Integer> tagNoList, final String contentType, final String regstrId);

    Optional<TagProfileEntity> findByTagProfileNoAndRegstrId(final Integer tagProfileNo, final String regstrId);
}
