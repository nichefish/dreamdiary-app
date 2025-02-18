package io.nicheblog.dreamdiary.extension.clsf.sectn.service.impl;

import io.nicheblog.dreamdiary.extension.cache.service.CacheEvictService;
import io.nicheblog.dreamdiary.extension.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.extension.clsf.sectn.entity.SectnEntity;
import io.nicheblog.dreamdiary.extension.clsf.sectn.mapstruct.SectnMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.sectn.model.SectnDto;
import io.nicheblog.dreamdiary.extension.clsf.sectn.repository.jpa.SectnRepository;
import io.nicheblog.dreamdiary.extension.clsf.sectn.service.SectnService;
import io.nicheblog.dreamdiary.extension.clsf.sectn.spec.SectnSpec;
import io.nicheblog.dreamdiary.extension.clsf.state.model.cmpstn.StateCmpstn;
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
@Service("sectnService")
@RequiredArgsConstructor
@Log4j2
public class SectnServiceImpl
        implements SectnService {

    @Getter
    private final SectnRepository repository;
    @Getter
    private final SectnSpec spec;
    @Getter
    private final SectnMapstruct mapstruct = SectnMapstruct.INSTANCE;

    private final CacheEvictService ehCacheEvictService;

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final SectnDto registDto) {
        if (registDto.getState() == null) registDto.setState(new StateCmpstn());
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postRegist(final SectnDto updatedDto) throws Exception {
        this.evictCache(updatedDto);
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postModify(final SectnDto updatedDto) throws Exception {
        this.evictCache(updatedDto);
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postDelete(final SectnDto deletedDto) throws Exception {
        this.evictCache(deletedDto);
    }

    /**
     * 관련 캐시 삭제.
     *
     * @param rslt 캐시 처리할 엔티티
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    public void evictCache(final SectnDto rslt) throws Exception {
        final String refContentType = rslt.getRefContentType();
        final Integer refPostNo = rslt.getRefPostNo();
        ehCacheEvictService.evictClsfCache(refContentType, refPostNo);
        EhCacheUtils.clearL2Cache(SectnEntity.class);
    }
}