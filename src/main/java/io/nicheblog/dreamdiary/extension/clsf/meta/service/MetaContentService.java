package io.nicheblog.dreamdiary.extension.clsf.meta.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.extension.clsf.meta.entity.MetaContentEntity;
import io.nicheblog.dreamdiary.extension.clsf.meta.entity.MetaEntity;
import io.nicheblog.dreamdiary.extension.clsf.meta.entity.MetaSmpEntity;
import io.nicheblog.dreamdiary.extension.clsf.meta.mapstruct.MetaContentMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.meta.model.MetaContentDto;
import io.nicheblog.dreamdiary.extension.clsf.meta.model.MetaContentParam;
import io.nicheblog.dreamdiary.extension.clsf.meta.model.MetaDto;
import io.nicheblog.dreamdiary.extension.clsf.meta.model.MetaSearchParam;
import io.nicheblog.dreamdiary.extension.clsf.meta.repository.jpa.MetaContentRepository;
import io.nicheblog.dreamdiary.extension.clsf.meta.repository.jpa.MetaSmpRepository;
import io.nicheblog.dreamdiary.extension.clsf.meta.spec.MetaContentSpec;
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
 * MetaContentService
 * <pre>
 *  메타-컨텐츠 서비스 모듈
 * </pre>
 *
 * @author nichefish
 */
@Service("metaContentService")
@RequiredArgsConstructor
@Log4j2
public class MetaContentService
        implements BaseCrudService<MetaContentDto, MetaContentDto, Integer, MetaContentEntity> {

    @Getter
    private final MetaContentRepository repository;
    @Getter
    private final MetaContentSpec spec;
    @Getter
    private final MetaContentMapstruct mapstruct = MetaContentMapstruct.INSTANCE;

    public MetaContentMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public MetaContentMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final MetaSmpRepository metaSmpRepository;

    private final ApplicationContext context;
    private MetaContentService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 특정 게시물에 대한 콘텐츠 메타 목록을 조회합니다.
     *
     * @param postNo 글 번호
     * @param contentType 컨텐츠 타입
     * @return {@link List} -- 메타 목록
     */
    @Cacheable(value = "metaContentEntityListByRef", key = "#postNo + '_' + #contentType.key")
    public List<MetaContentEntity> getListEntityByRefWithCache(final Integer postNo, final ContentType contentType) throws Exception {
        final MetaContentParam param = MetaContentParam.builder()
                .refPostNo(postNo)
                .refContentType(contentType.key)
                .build();
        return this.getSelf().getListEntityMeta(param);
    }

    /**
     * 특정 게시물에 대한 콘텐츠 메타 목록을 조회합니다.
     *
     * @param param 파라미터
     * @return {@link List} -- 메타 목록
     */
    public List<MetaContentEntity> getListEntityMeta(final MetaContentParam param) throws Exception {
        final List<MetaContentEntity> entityList = this.getSelf().getListEntity(param);
        return entityList.stream()
                .peek(entity -> {
                    final Integer metaNo = entity.getRefMetaNo();
                    final MetaSmpEntity meta = this.getSelf().getMetaSmpDtlEntity(metaNo);
                    entity.setMeta(meta);
                    entity.setMetaNm(meta.getMetaNm());
                    entity.setCtgr(meta.getCtgr());
                    entity.setLabel(meta.getLabel());
                })
                .collect(Collectors.toList());
    }

    /**
     * 메타 조회
     *
     * @param metaNo 메타 번호
     * @return {@link List} -- 메타 목록
     */
    @Cacheable(value = "metaSmpDtlEntity", key = "#metaNo.toString()")
    public MetaSmpEntity getMetaSmpDtlEntity(final Integer metaNo) {
        final Optional<MetaSmpEntity> rsWrapper = metaSmpRepository.findById(metaNo);

        return rsWrapper.orElse(null);
    }

    /**
     * 특정 게시물에 대한 콘텐츠 메타 목록을 조회합니다.
     *
     * @param clsfKey 참조 복합키 정보 (BaseClsfKey)
     * @return {@link List} -- 메타 목록
     */
    @Transactional(readOnly = true)
    public List<MetaDto> getMetaStrListByClsfKey(final BaseClsfKey clsfKey) throws Exception {
        final List<MetaContentEntity> entityList = this.getSelf().getListEntityByRefWithCache(clsfKey.getPostNo(), clsfKey.getContentTypeEnum());
        if (CollectionUtils.isEmpty(entityList)) return new ArrayList<>();

        return entityList.stream()
                .map(meta -> MetaDto.builder().metaNo(meta.getRefMetaNo()).metaNm(meta.getMetaNm()).value(meta.getValue()).build())
                .collect(Collectors.toList());
    }

    /**
     * 특정 게시물에 대해 메타 정보와 연결되지 않는 메타-컨텐츠 삭제.
     *
     * @param clsfKey 참조 복합키 정보 (BaseClsfKey)
     * @param obsoleteMetaList 삭제할 메타 목록
     */
    @Transactional
    public void delObsoleteMetaContents(final BaseClsfKey clsfKey, final List<MetaDto> obsoleteMetaList) throws Exception {
        obsoleteMetaList.forEach(meta -> {
            final MetaContentParam param = MetaContentParam.builder()
                    .refPostNo(clsfKey.getPostNo())
                    .refContentType(clsfKey.getContentType())
                    .metaNm(meta.getMetaNm())
                    .ctgr(meta.getCtgr())
                    .label(meta.getLabel())
                    .regstrId(AuthUtils.getLgnUserId())
                    .build();
            repository.deleteObsoleteMetaContents(param);
            // 메타 캐시 처리
        });
    }

    /**
     * 특정 게시물에 대해 메타-컨텐츠 목록 추가.
     *
     * @param clsfKey 참조 복합키 정보 (BaseClsfKey)
     * @param rsList  처리할 메타 엔티티 목록 (List<MetaEntity>)
     * @return {@link List} -- 등록된 메타-컨텐츠 엔티티 목록
     */
    @Transactional
    public List<MetaContentEntity> addMetaContents(final BaseClsfKey clsfKey, final List<MetaEntity> rsList) throws Exception {
        final List<MetaContentEntity> metaContentList = rsList.stream()
                .map(meta -> new MetaContentEntity(meta.getMetaNo(), clsfKey, meta.getValue(), meta.getUnit()))
                .collect(Collectors.toList());
        return this.registAll(metaContentList);
    }

    public List<MetaContentDto> getMyListDtoByRefMetaNo(Integer refMetaNo, String lgnUserId) throws Exception {

        return this.getSelf().getListDto(MetaSearchParam.builder().refMetaNo(refMetaNo).lgnUserId(lgnUserId).build());
    }
}