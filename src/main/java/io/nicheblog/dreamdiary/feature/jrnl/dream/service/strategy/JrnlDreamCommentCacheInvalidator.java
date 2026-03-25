package io.nicheblog.dreamdiary.feature.jrnl.dream.service.strategy;

import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.comment.cache.CommentCacheInvalidator;
import io.nicheblog.dreamdiary.feature.jrnl._shared.handler.JrnlCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.jrnl._shared.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.feature.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.feature.jrnl.dream.service.JrnlDreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * CommentCacheInvalidator
 * <pre>
 *  댓글 캐시 무효화 방식을 정의.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class JrnlDreamCommentCacheInvalidator
        implements CommentCacheInvalidator {

    private final JrnlDreamService jrnlDreamService;
    private final JrnlCacheEvictWorker jrnlCacheEvictWorker;

    /**
     * 해당 ContentType을 이 전략이 처리할 수 있는지 여부를 반환한다.
     *
     * @param refContentType 콘텐츠 타입
     * @return 처리 가능 여부
     */
    @Override
    public boolean supports(final ContentType refContentType) {
        return ContentType.JRNL_DREAM.equals(refContentType);
    }

    /**
     * 주어진 게시글에 대한 캐시를 무효화한다.
     *
     * @param refPostNo 참조 대상 게시글 번호
     * @throws Exception 캐시 무효화 과정에서 발생할 수 있는 예외
     */
    @Override
    public void invalidate(final Integer refPostNo) throws Exception {
        final JrnlDreamDto jrnlDreamDto = jrnlDreamService.getDtlDto(refPostNo);
        final JrnlCacheEvictParam param = JrnlCacheEvictParam.of(jrnlDreamDto);
        jrnlCacheEvictWorker.evictAfterCommit(param, ContentType.JRNL_DREAM);
    }
}
