package io.nicheblog.dreamdiary.global._common._clsf.comment.service.impl;

import io.nicheblog.dreamdiary.global._common._clsf.comment.entity.CommentEntity;
import io.nicheblog.dreamdiary.global._common._clsf.comment.mapstruct.CommentMapstruct;
import io.nicheblog.dreamdiary.global._common._clsf.comment.repository.jpa.CommentRepository;
import io.nicheblog.dreamdiary.global._common._clsf.comment.service.CommentService;
import io.nicheblog.dreamdiary.global._common._clsf.comment.spec.CommentSpec;
import io.nicheblog.dreamdiary.global._common.cache.service.CacheEvictService;
import io.nicheblog.dreamdiary.global._common.cache.util.EhCacheUtils;
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
     * 등록 후처리. (override)
     * 
     * @param rslt - 등록된 엔티티
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    public void postRegist(final CommentEntity rslt) throws Exception {
        // 관련 캐시 삭제
        this.evictClsfCache(rslt);
    }

    /**
     * 수정 후처리. (override)
     *
     * @param rslt - 수정된 엔티티
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    public void postModify(final CommentEntity rslt) throws Exception {
        // 관련 캐시 삭제
        this.evictClsfCache(rslt);
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param rslt - 삭제된 엔티티
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    public void postDelete(final CommentEntity rslt) throws Exception {
        // 관련 캐시 삭제
        this.evictClsfCache(rslt);

        // TODO: 관련 엔티티 삭제?
    }

    /**
     * 관련 캐시 삭제.
     *
     * @param rslt 캐시 처리할 엔티티
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    public void evictClsfCache(final CommentEntity rslt) throws Exception {
        String refContentType = rslt.getRefContentType();
        Integer refPostNo = rslt.getRefPostNo();
        ehCacheEvictService.evictClsfCache(refContentType, refPostNo);
        EhCacheUtils.clearL2Cache(CommentEntity.class);
    }
}