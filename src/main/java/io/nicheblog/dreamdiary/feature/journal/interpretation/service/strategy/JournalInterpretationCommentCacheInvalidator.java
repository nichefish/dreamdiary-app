package io.nicheblog.dreamdiary.feature.journal.interpretation.service.strategy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.comment.cache.CommentCacheInvalidator;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.feature.journal.interpretation.model.JournalInterpretationDto;
import io.nicheblog.dreamdiary.feature.journal.interpretation.service.JournalInterpretationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * JournalInterpretationCommentCacheInvalidator
 * <pre>
 *  댓글 캐시 무효화 방식 정의.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class JournalInterpretationCommentCacheInvalidator
        implements CommentCacheInvalidator {

    private final JournalInterpretationService journalInterpretationService;
    private final JournalCacheEvictWorker journalCacheEvictWorker;

    /**
     * 해당 ContentType을 이 전략이 처리할 수 있는지 여부를 반환한다.
     *
     * @param refContentType 콘텐츠 타입
     * @return 처리 가능 여부
     */
    @Override
    public boolean supports(final ContentType refContentType) {
        return ContentType.JOURNAL_INTERPRETATION.equals(refContentType);
    }

    /**
     * 주어진 게시글에 대한 캐시를 무효화한다.
     *
     * @param refId 참조 대상 게시글 번호
     * @throws Exception 캐시 무효화 과정에서 발생할 수 있는 예외
     */
    @Override
    public void invalidate(final Integer refId) throws Exception {
        final JournalInterpretationDto journalInterpretationDto = journalInterpretationService.getDtlDto(refId);
        final JournalCacheEvictParam param = JournalCacheEvictParam.of(journalInterpretationDto);
        journalCacheEvictWorker.evictAfterCommit(param, ContentType.JOURNAL_INTERPRETATION);
    }
}

