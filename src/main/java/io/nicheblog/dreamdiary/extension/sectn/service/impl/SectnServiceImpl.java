package io.nicheblog.dreamdiary.extension.sectn.service.impl;

import io.nicheblog.dreamdiary.extension.sectn.entity.SectnEntity;
import io.nicheblog.dreamdiary.extension.sectn.mapstruct.SectnMapstruct;
import io.nicheblog.dreamdiary.extension.sectn.model.SectnDto;
import io.nicheblog.dreamdiary.extension.sectn.repository.jpa.SectnRepository;
import io.nicheblog.dreamdiary.extension.sectn.service.SectnService;
import io.nicheblog.dreamdiary.extension.sectn.spec.SectnSpec;
import io.nicheblog.dreamdiary.extension.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.global._common.cache.service.CacheEvictService;
import io.nicheblog.dreamdiary.global._common.cache.util.EhCacheUtils;
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
     * @param dto 등록할 객체
     */
    @Override
    public void preRegist(final SectnDto dto) {
        if (dto.getState() == null) dto.setState(new StateCmpstn());
    }

    /**
     * 관련 캐시 삭제.
     *
     * @param rslt 캐시 처리할 엔티티
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    public void evictCache(final SectnEntity rslt) throws Exception {
        final String refContentType = rslt.getRefContentType();
        final Integer refPostNo = rslt.getRefPostNo();
        ehCacheEvictService.evictClsfCache(refContentType, refPostNo);
        EhCacheUtils.clearL2Cache(SectnEntity.class);
    }
}