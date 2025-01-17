package io.nicheblog.dreamdiary.global._common._clsf.tag.service;

import io.nicheblog.dreamdiary.global._common._clsf.tag.entity.ContentTagEntity;
import io.nicheblog.dreamdiary.global._common._clsf.tag.entity.TagEntity;
import io.nicheblog.dreamdiary.global._common._clsf.tag.mapstruct.ContentTagMapstruct;
import io.nicheblog.dreamdiary.global._common._clsf.tag.model.ContentTagDto;
import io.nicheblog.dreamdiary.global._common._clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.global._common._clsf.tag.repository.jpa.ContentTagRepository;
import io.nicheblog.dreamdiary.global._common._clsf.tag.spec.ContentTagSpec;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseCrudService;

import java.util.List;

/**
 * ContentTagService
 * <pre>
 *  컨텐츠-태그 서비스 인터페이스
 * </pre>
 *
 * @author nichefish
 */
public interface ContentTagService
        extends BaseCrudService<ContentTagDto, ContentTagDto, Integer, ContentTagEntity, ContentTagRepository, ContentTagSpec, ContentTagMapstruct> {

    /**
     * 특정 게시물에 대한 콘텐츠 태그 목록을 조회합니다.
     *
     * @param clsfKey 참조 복합키 정보 (BaseClsfKey)
     * @return {@link List} -- 태그 목록
     */
    List<TagDto> getTagStrListByClsfKey(final BaseClsfKey clsfKey);

    /**
     * 특정 게시물에 대해 태그 정보와 연결되지 않는 컨텐츠 태그 삭제.
     *
     * @param clsfKey 참조 복합키 정보 (BaseClsfKey)
     * @param obsoleteTagList 삭제할 태그 목록
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    void delObsoleteContentTags(final BaseClsfKey clsfKey, final List<TagDto> obsoleteTagList) throws Exception;

    /**
     * 특정 게시물에 대해 컨텐츠 태그 목록 추가.
     *
     * @param clsfKey 참조 복합키 정보 (BaseClsfKey)
     * @param rsList 처리할 태그 엔티티 목록 (List<TagEntity>)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    void addContentTags(final BaseClsfKey clsfKey, final List<TagEntity> rsList) throws Exception;

    /**
     * 특정 게시물에 대해 기존 콘텐츠 태그를 모두 삭제합니다.
     *
     * @param clsfKey 참조 복합키 정보 (BaseClsfKey)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    void delExistingContentTags(final BaseClsfKey clsfKey) throws Exception;

    /**
     * 콘텐츠 타입에 따른 캐시 이름 반환 :: 메소드 분리
     *
     * @param contentType 콘텐츠 유형 (String)
     * @return {@link String} -- 해당 콘텐츠 유형에 맞는 캐시 이름.
     */
    String getCacheNameByContentType(final String contentType);

    /**
     * 기간에 따른 컨텐츠 태그 캐시 삭제 :: 메소드 분리
     *
     * @param cacheName 캐시 이름
     * @param tagNo 태그 번호 (key)
     */
    void evictMyCacheForPeriod(final String cacheName, final Integer tagNo);
}
