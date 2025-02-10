package io.nicheblog.dreamdiary.extension.clsf.comment.service.impl;

import io.nicheblog.dreamdiary.extension.clsf.comment.entity.CommentEntity;
import io.nicheblog.dreamdiary.extension.clsf.comment.mapstruct.CommentMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.comment.repository.jpa.CommentRepository;
import io.nicheblog.dreamdiary.extension.clsf.comment.service.CommentService;
import io.nicheblog.dreamdiary.extension.clsf.comment.spec.CommentSpec;
import io.nicheblog.dreamdiary.extension.cache.service.CacheEvictService;
import io.nicheblog.dreamdiary.extension.cache.util.EhCacheUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * CommentService
 * <pre>
 *  댓글 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("commentService")
@RequiredArgsConstructor
public class CommentServiceImpl
        implements CommentService {

    @Getter
    private final CommentRepository repository;
    @Getter
    private final CommentSpec spec;
    @Getter
    private final CommentMapstruct mapstruct = CommentMapstruct.INSTANCE;

    private final CacheEvictService ehCacheEvictService;

    /**
     * 관련 캐시 삭제.
     *
     * @param rslt 캐시 처리할 엔티티
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    public void evictCache(final CommentEntity rslt) throws Exception {
        final String refContentType = rslt.getRefContentType();
        final Integer refPostNo = rslt.getRefPostNo();
        ehCacheEvictService.evictClsfCache(refContentType, refPostNo);
        EhCacheUtils.clearL2Cache(CommentEntity.class);
    }
}