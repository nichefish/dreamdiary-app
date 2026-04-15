package io.nicheblog.dreamdiary.feature.journal.day.service.strategy;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.comment.cache.CommentCacheInvalidator;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.service.JournalDayService;
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
public class JournalDayCommentCacheInvalidator
        implements CommentCacheInvalidator {

    private final JournalDayService journalDayService;
    private final JournalCacheEvictWorker journalCacheEvictWorker;

    /**
     * 해당 ContentType을 이 전략이 처리할 수 있는지 여부를 반환한다.
     *
     * @param refContentType 콘텐츠 타입
     * @return 처리 가능 여부
     */
    @Override
    public boolean supports(final ContentType refContentType) {
        return ContentType.JOURNAL_DAY.equals(refContentType);
    }

    /**
     * 주어진 게시글에 대한 캐시를 무효화한다.
     *
     * @param refId 참조 대상 게시글 번호
     * @throws Exception 캐시 무효화 과정에서 발생할 수 있는 예외
     */
    @Override
    public void invalidate(final Integer refId) throws Exception {
        final JournalDayDto journalDayDto = journalDayService.getDtlDto(refId);
        final JournalCacheEvictParam param = JournalCacheEvictParam.of(journalDayDto);
        journalCacheEvictWorker.evictAfterCommit(param, ContentType.JOURNAL_DAY);
    }
}

