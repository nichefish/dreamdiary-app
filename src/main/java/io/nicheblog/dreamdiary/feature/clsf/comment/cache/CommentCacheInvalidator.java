package io.nicheblog.dreamdiary.feature.clsf.comment.cache;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;

/**
 * CommentCacheInvalidator
 * <pre>
 *  콘텐츠 타입별로 댓글 캐시 무효화 방식을 정의하는 전략 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface CommentCacheInvalidator {

    /**
     * 해당 ContentType을 이 전략이 처리할 수 있는지 여부를 반환한다.
     *
     * @param refContentType 콘텐츠 타입
     * @return 처리 가능 여부
     */
    boolean supports(final ContentType refContentType);

    /**
     * 주어진 게시글에 대한 캐시를 무효화한다.
     *
     * @param refPostNo 참조 대상 게시글 번호
     * @throws Exception 캐시 무효화 과정에서 발생할 수 있는 예외
     */
    void invalidate(final Integer refPostNo) throws Exception;
}
