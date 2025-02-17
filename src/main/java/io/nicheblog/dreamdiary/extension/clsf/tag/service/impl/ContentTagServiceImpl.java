package io.nicheblog.dreamdiary.extension.clsf.tag.service.impl;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.extension.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.extension.clsf.tag.entity.ContentTagEntity;
import io.nicheblog.dreamdiary.extension.clsf.tag.entity.TagEntity;
import io.nicheblog.dreamdiary.extension.clsf.tag.entity.TagSmpEntity;
import io.nicheblog.dreamdiary.extension.clsf.tag.mapstruct.ContentTagMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.ContentTagParam;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.extension.clsf.tag.repository.jpa.ContentTagRepository;
import io.nicheblog.dreamdiary.extension.clsf.tag.repository.jpa.TagSmpRepository;
import io.nicheblog.dreamdiary.extension.clsf.tag.service.ContentTagService;
import io.nicheblog.dreamdiary.extension.clsf.tag.spec.ContentTagSpec;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ContentTagService
 * <pre>
 *  컨텐츠-태그 서비스 모듈
 * </pre>
 *
 * @author nichefish
 */
@Service("contentTagService")
@RequiredArgsConstructor
@Log4j2
public class ContentTagServiceImpl
        implements ContentTagService {

    @Getter
    private final ContentTagRepository repository;
    @Getter
    private final ContentTagSpec spec;
    @Getter
    private final ContentTagMapstruct mapstruct = ContentTagMapstruct.INSTANCE;

    private final TagSmpRepository tagSmpRepository;

    private final ApplicationContext context;
    private ContentTagServiceImpl getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 특정 게시물에 대한 콘텐츠 태그 목록을 조회합니다.
     *
     * @param postNo 글 번호
     * @param contentType 컨텐츠 타입
     * @return {@link List} -- 태그 목록
     */
    @Cacheable(value = "contentTagEntityListByRef", key = "#postNo + '_' + #contentType.key")
    public List<ContentTagEntity> getListEntityByRefWithCache(final Integer postNo, final ContentType contentType) throws Exception {
        final ContentTagParam param = ContentTagParam.builder()
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
    public List<ContentTagEntity> getListEntityTag(final ContentTagParam param) throws Exception {
        final List<ContentTagEntity> entityList = this.getSelf().getListEntity(param);
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
    @Override
    @Transactional(readOnly = true)
    public List<TagDto> getTagStrListByClsfKey(final BaseClsfKey clsfKey) throws Exception {
        final List<ContentTagEntity> entityList = this.getSelf().getListEntityByRefWithCache(clsfKey.getPostNo(), clsfKey.getContentTypeEnum());
        if (CollectionUtils.isEmpty(entityList)) return new ArrayList<>();

        return entityList.stream()
                .map(tag -> new TagDto(tag.getRefTagNo(), tag.getTagNm(), tag.getCtgr()))
                .collect(Collectors.toList());
    }

    /**
     * 특정 게시물에 대해 태그 정보와 연결되지 않는 컨텐츠 태그 삭제.
     *
     * @param clsfKey 참조 복합키 정보 (BaseClsfKey)
     * @param obsoleteTagList 삭제할 태그 목록
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    @Transactional
    public void delObsoleteContentTags(final BaseClsfKey clsfKey, final List<TagDto> obsoleteTagList) throws Exception {
        final String contentType = clsfKey.getContentType();
        obsoleteTagList.forEach(tag -> {
            final ContentTagParam param = ContentTagParam.builder()
                    .refPostNo(clsfKey.getPostNo())
                    .refContentType(clsfKey.getContentType())
                    .tagNm(tag.getTagNm())
                    .ctgr(tag.getCtgr())
                    .regstrId(AuthUtils.getLgnUserId())
                    .build();
            repository.deleteObsoleteContentTags(param);
            // 태그 캐시 처리
            final String cacheName = this.getCacheNameByContentType(contentType);
            this.evictMyCacheForPeriod(cacheName, tag.getTagNo());
        });
    }

    /**
     * 특정 게시물에 대해 컨텐츠 태그 목록 추가.
     *
     * @param clsfKey 참조 복합키 정보 (BaseClsfKey)
     * @param rsList  처리할 태그 엔티티 목록 (List<TagEntity>)
     * @return {@link List} -- 등록된 컨텐츠 태그 엔티티 목록
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    @Transactional
    public List<ContentTagEntity> addContentTags(final BaseClsfKey clsfKey, final List<TagEntity> rsList) throws Exception {
        final List<ContentTagEntity> contentTagList = rsList.stream()
                .map(tag -> new ContentTagEntity(tag.getTagNo(), clsfKey))
                .collect(Collectors.toList());
        return this.registAll(contentTagList);
    }

    /**
     * 컨텐츠 태그 bulk 등록 후처리. (override)
     *
     * @param entityList 등록된 콘텐츠 태그 엔티티 목록
     */
    @Override
    public void evictCache(final List<ContentTagEntity> entityList) {
        // 태그 개수 캐시 초기화
        entityList.forEach(entity -> {
            final String contentType = entity.getRefContentType();
            final String cacheName = this.getCacheNameByContentType(contentType);
            if (cacheName.isEmpty()) return;
            final Integer tagNo = entity.getRefTagNo();;
            this.evictMyCacheForPeriod(cacheName, tagNo);
        });
    }

    /**
     * 콘텐츠 타입에 따른 캐시 이름 반환 :: 메소드 분리
     *
     * @param contentType 콘텐츠 유형 (String)
     * @return {@link String} -- 해당 콘텐츠 유형에 맞는 캐시 이름.
     */
    @Override
    public String getCacheNameByContentType(final String contentType) {
        if (ContentType.JRNL_DAY.key.equals(contentType)) {
            return "myCountDaySize";
        } else if (ContentType.JRNL_DIARY.key.equals(contentType)) {
            return "myCountDiarySize";
        } else if (ContentType.JRNL_DREAM.key.equals(contentType)) {
            return "myCountDreamSize";
        }
        return "";
    }

    /**
     * 기간에 따른 컨텐츠 태그 캐시 삭제 :: 메소드 분리
     *
     * @param cacheName 캐시 이름
     * @param tagNo 태그 번호 (key)
     */
    @Override
    public void evictMyCacheForPeriod(final String cacheName, final Integer tagNo) {
        try {
            final int currYy = DateUtils.getCurrYy();
            for (int yy = 2010; yy <= currYy; yy++) {
                for (int mnth = 1; mnth <= 12; mnth++) {
                    final String cacheKey = tagNo + "_" + yy + "_" + mnth;
                    EhCacheUtils.evictMyCache(cacheName, cacheKey);
                }
            }
            EhCacheUtils.evictMyCache(cacheName, tagNo + "_9999_99");
        } catch (final Exception e) {
            throw new RuntimeException("Failed to evict cache for tagNo: " + tagNo, e);
        }
    }
}
