package io.nicheblog.dreamdiary.feature.attachable.sectn.service;

import io.nicheblog.dreamdiary.feature.attachable.sectn.entity.SectnEntity;
import io.nicheblog.dreamdiary.feature.attachable.sectn.mapstruct.SectnMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.sectn.model.SectnDto;
import io.nicheblog.dreamdiary.feature.attachable.sectn.repository.jpa.SectnRepository;
import io.nicheblog.dreamdiary.feature.attachable.sectn.spec.SectnSpec;
import io.nicheblog.dreamdiary.feature.file.service.BaseMultipartWritableService;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import io.nicheblog.dreamdiary.infrastructure.cache.service.CacheEvictService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * SectnService
 * <pre>
 *  단락 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class SectnService
        implements BaseDtoWritableService<SectnDto, SectnDto, Integer, SectnEntity>, BaseMultipartWritableService<SectnDto, SectnDto, Integer, SectnEntity> {

    @Getter
    private final SectnRepository repository;
    @Getter
    private final SectnSpec spec;
    @Getter
    private final SectnMapstruct mapstruct = SectnMapstruct.INSTANCE;

    public SectnMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public SectnMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final CacheEvictService ehCacheEvictService;

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final SectnDto updatedDto) throws Exception {
        this.evictCache(updatedDto);
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final SectnDto postDto, final SectnDto updatedDto) throws Exception {
        this.evictCache(updatedDto);
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void postDelete(final SectnDto deletedDto) throws Exception {
        this.evictCache(deletedDto);
    }

    /**
     * 관련 캐시 삭제.
     *
     * @param rslt 캐시 처리할 엔티티
     */
    public void evictCache(final SectnDto rslt) throws Exception {
        final String refContentType = rslt.getRefContentType();
        final Integer refId = rslt.getRefId();
        ehCacheEvictService.evictAttachableCache(refContentType, refId);
    }
}

