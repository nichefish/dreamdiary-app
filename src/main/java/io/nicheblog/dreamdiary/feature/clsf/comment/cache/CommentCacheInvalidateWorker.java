package io.nicheblog.dreamdiary.feature.clsf.comment.cache;

import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.global.util.TransactionHookUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CommentCacheInvalidateWorker
 * <p>
 *  댓글(Comment) 변경 이후, 관련 캐시를 무효화하기 위한 전략들을 실행하는 워커.
 *  단순 캐시 삭제가 아니라, 트랜잭션 경계 이후 일관성을 보장하기 위한 후처리 레이어 역할을 수행한다.
 * </p>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class CommentCacheInvalidateWorker {

    private final List<CommentCacheInvalidator> strategies;

    /**
     * 트랜잭션 커밋 이후에 캐시 무효화를 수행하도록 예약한다.
     * 트랜잭션이 활성 상태일 경우, {@code afterCommit()} 시점에 invalidate가 실행되도록 등록한다.
     * 트랜잭션이 없거나 비활성 상태일 경우 즉시 invalidate를 수행한다.
     *
     * @param refPostNo      참조 대상 게시글 번호
     * @param refContentType 콘텐츠 타입 (전략 선택 기준)
     * @throws Exception invalidate 실행 중 발생 가능한 예외 (즉시 실행 경로에서만 전파됨)
     */
    public void invalidateAfterCommit(final Integer refPostNo, final ContentType refContentType) throws Exception {
        if (refPostNo == null || refContentType == null || ContentType.DEFAULT.equals(refContentType)) return;

        TransactionHookUtils.runAfterCommitOrNow(
                () -> this.invalidate(refPostNo, refContentType),
                e -> log.error("Comment cache invalidation failed [{}:{}]: {}", refContentType, refPostNo, e.getMessage(), e)
        );
    }

    /**
     * 콘텐츠 타입에 맞는 캐시 무효화 전략을 선택하여 실행한다.
     * 등록된 {@link CommentCacheInvalidator} 목록을 순회하며, {@code supports(ContentType)} 조건을 만족하는 첫 번째 전략을 실행한다.
     *
     * @param refPostNo      참조 대상 게시글 번호
     * @param refContentType 콘텐츠 타입
     * @throws Exception 전략 실행 중 발생 가능한 예외
     */
    public void invalidate(final Integer refPostNo, final ContentType refContentType) throws Exception {
        for (final CommentCacheInvalidator strategy : strategies) {
            if (!strategy.supports(refContentType)) continue;

            strategy.invalidate(refPostNo);
            return;
        }
        log.warn("No Comment cache invalidation strategy found for ContentType: {}", refContentType);
    }
}
