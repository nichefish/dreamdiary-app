package io.nicheblog.dreamdiary.extension.clsf.tag.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.extension.clsf.tag.entity.TagContentEntity;
import io.nicheblog.dreamdiary.extension.clsf.tag.entity.TagEntity;
import io.nicheblog.dreamdiary.extension.clsf.tag.entity.TagSmpEntity;
import io.nicheblog.dreamdiary.extension.clsf.tag.mapstruct.TagContentMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.TagContentDto;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.TagContentParam;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.extension.clsf.tag.repository.jpa.TagContentRepository;
import io.nicheblog.dreamdiary.extension.clsf.tag.repository.jpa.TagSmpRepository;
import io.nicheblog.dreamdiary.extension.clsf.tag.spec.TagContentSpec;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseCrudService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TagContentService
 * <pre>
 *  태그-컨텐츠 서비스 모듈
 * </pre>
 *
 * @author nichefish
 */
@Service("tagContentService")
@RequiredArgsConstructor
@Log4j2
public class TagContentService
        implements BaseCrudService<TagContentDto, TagContentDto, Integer, TagContentEntity> {

    @Getter
    private final TagContentRepository repository;
    @Getter
    private final TagContentSpec spec;
    @Getter
    private final TagContentMapstruct mapstruct = TagContentMapstruct.INSTANCE;

    public TagContentMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public TagContentMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final TagSmpRepository tagSmpRepository;

    private final ApplicationContext context;
    private TagContentService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 특정 게시물에 대한 콘텐츠 태그 목록을 조회합니다.
     *
     * @param postNo 글 번호
     * @param contentType 컨텐츠 타입
     * @return {@link List} -- 태그 목록
     */
    @Cacheable(value = "tagContentEntityListByRef", key = "#postNo + '_' + #contentType.key")
    public List<TagContentEntity> getListEntityByRefWithCache(final Integer postNo, final ContentType contentType) throws Exception {
        final TagContentParam param = TagContentParam.builder()
                .refPostNo(postNo)
                .refContentType(contentType.key)
                .build();
        return this.getSelf().getListEntityTag(param);
    }

    /**
     * 특정 게시물에 대한 콘텐츠 태그 목록을 조회합니다.
     *
     * @param param 파라미터
     * @return {@link List} -- 태그 목록
     */
    public List<TagContentEntity> getListEntityTag(final TagContentParam param) throws Exception {
        final List<TagContentEntity> entityList = this.getSelf().getListEntity(param);
        return entityList.stream()
                .peek(entity -> {
                    final Integer tagNo = entity.getRefTagNo();
                    final TagSmpEntity tag = this.getSelf().getTagSmpDtlEntity(tagNo);
                    entity.setTag(tag);
                    entity.setTagNm(tag.getTagNm());
                    entity.setCtgr(tag.getCtgr());
                })
                .collect(Collectors.toList());
    }

    /**
     * 태그 조회
     *
     * @param tagNo 태그 번호
     * @return {@link List} -- 태그 목록
     */
    @Cacheable(value = "tagSmpDtlEntity", key = "#tagNo.toString()")
    public TagSmpEntity getTagSmpDtlEntity(final Integer tagNo) {
        final Optional<TagSmpEntity> rsWrapper = tagSmpRepository.findById(tagNo);

        return rsWrapper.orElse(null);
    }

    /**
     * 특정 게시물에 대한 콘텐츠 태그 목록을 조회합니다.
     *
     * @param clsfKey 참조 복합키 정보 (BaseClsfKey)
     * @return {@link List} -- 태그 목록
     */
    @Transactional(readOnly = true)
    public List<TagDto> getTagStrListByClsfKey(final BaseClsfKey clsfKey) throws Exception {
        final List<TagContentEntity> entityList = this.getSelf().getListEntityByRefWithCache(clsfKey.getPostNo(), clsfKey.getContentTypeEnum());
        if (CollectionUtils.isEmpty(entityList)) return new ArrayList<>();

        return entityList.stream()
                .map(tag -> new TagDto(tag.getRefTagNo(), tag.getTagNm(), tag.getCtgr()))
                .collect(Collectors.toList());
    }

    /**
     * 특정 게시물에 대해 태그 정보와 연결되지 않는 태그-컨텐츠 삭제.
     *
     * @param clsfKey 참조 복합키 정보 (BaseClsfKey)
     * @param obsoleteTagList 삭제할 태그 목록
     */
    @Transactional
    public void delObsoleteTagContents(final BaseClsfKey clsfKey, final List<TagDto> obsoleteTagList) throws Exception {
        final String contentType = clsfKey.getContentType();
        obsoleteTagList.forEach(tag -> {
            final TagContentParam param = TagContentParam.builder()
                    .refPostNo(clsfKey.getPostNo())
                    .refContentType(clsfKey.getContentType())
                    .tagNm(tag.getTagNm())
                    .ctgr(tag.getCtgr())
                    .regstrId(AuthUtils.getLgnUserId())
                    .build();
            repository.deleteObsoleteTagContents(param);
            // 태그 캐시 처리
        });
    }

    /**
     * 특정 게시물에 대해 태그-컨텐츠 목록 추가.
     *
     * @param clsfKey 참조 복합키 정보 (BaseClsfKey)
     * @param rsList  처리할 태그 엔티티 목록 (List<TagEntity>)
     * @return {@link List} -- 등록된 태그-컨텐츠 엔티티 목록
     */
    @Transactional
    public List<TagContentEntity> addTagContents(final BaseClsfKey clsfKey, final List<TagEntity> rsList) throws Exception {
        final List<TagContentEntity> tagContentList = rsList.stream()
                .map(tag -> new TagContentEntity(tag.getTagNo(), clsfKey))
                .collect(Collectors.toList());
        return this.registAll(tagContentList);
    }
}