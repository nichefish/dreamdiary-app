package io.nicheblog.dreamdiary.feature.journal.entry.service.strategy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.comment.cache.CommentCacheInvalidator;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.service.JournalEntryService;
import io.nicheblog.dreamdiary.feature.journal.entry.service.policy.JournalEntryTypePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JournalEntryCommentCacheInvalidator
        implements CommentCacheInvalidator {

    private final JournalEntryService journalEntryService;
    private final JournalCacheEvictWorker journalCacheEvictWorker;

    /**
     * 해당 콘텐츠 타입을 이 무효화기가 지원하는지 확인한다.
     *
     * @param refContentType 참조 콘텐츠 타입
     * @return 지원 여부
     */
    @Override
    public boolean supports(final ContentType refContentType) {
        return JournalEntryTypePolicy.isEntryType(refContentType);
    }

    /**
     * 콘텐츠 타입 정보가 없는 호출은 지원하지 않는다.
     *
     * @param refId 참조 ID
     * @throws Exception 무효화 처리 중 예외
     */
    @Override
    public void invalidate(final Integer refId) throws Exception {
        throw new UnsupportedOperationException("Journal entry comment cache invalidation requires refContentType.");
    }

    /**
     * 댓글 변경 시 엔트리 상세/연관 캐시를 비동기 무효화한다.
     *
     * @param refId 참조 ID
     * @param refContentType 참조 콘텐츠 타입
     * @throws Exception 무효화 처리 중 예외
     */
    @Override
    public void invalidate(final Integer refId, final ContentType refContentType) throws Exception {
        final JournalEntryDto journalEntryDto = journalEntryService.getDtlDto(refId);
        final JournalCacheEvictParam param = JournalCacheEvictParam.of(journalEntryDto);
        journalCacheEvictWorker.evictAfterCommit(param, refContentType);
    }
}
