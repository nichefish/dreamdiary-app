package io.nicheblog.dreamdiary.feature.jrnl.sumry.service.strategy;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.comment.cache.CommentCacheInvalidator;
import io.nicheblog.dreamdiary.feature.jrnl._shared.handler.JrnlCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.jrnl._shared.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.model.JrnlSumryReviewDto;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.service.JrnlSumryReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * JrnlSumryReviewCommentCacheInvalidator
 * <pre>
 *  댓글 캐시 무효화 방식 정의.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class JrnlSumryReviewCommentCacheInvalidator
        implements CommentCacheInvalidator {

    private final JrnlSumryReviewService jrnlSumryReviewService;
    private final JrnlCacheEvictWorker jrnlCacheEvictWorker;

    /**
     * 해당 ContentType을 이 전략이 처리할 수 있는지 여부를 반환한다.
     *
     * @param refContentType 콘텐츠 타입
     * @return 처리 가능 여부
     */
    @Override
    public boolean supports(final ContentType refContentType) {
        return ContentType.JRNL_SUMRY_REVIEW.equals(refContentType);
    }

    /**
     * 주어진 게시글에 대한 캐시를 무효화한다.
     *
     * @param refPostNo 참조 대상 게시글 번호
     * @throws Exception 캐시 무효화 과정에서 발생할 수 있는 예외
     */
    @Override
    public void invalidate(final Integer refPostNo) throws Exception {
        final JrnlSumryReviewDto jrnlSumryReviewDto = jrnlSumryReviewService.getDtlDto(refPostNo);
        final JrnlCacheEvictParam param = JrnlCacheEvictParam.of(jrnlSumryReviewDto);
        jrnlCacheEvictWorker.evictAfterCommit(param, ContentType.JRNL_SUMRY_REVIEW);
    }
}
