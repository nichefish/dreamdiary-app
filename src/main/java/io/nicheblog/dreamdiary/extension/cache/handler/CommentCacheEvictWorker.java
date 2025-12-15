package io.nicheblog.dreamdiary.extension.cache.handler;

import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.domain.jrnl.day.service.JrnlDayService;
import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.domain.jrnl.diary.service.JrnlDiaryService;
import io.nicheblog.dreamdiary.domain.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.domain.jrnl.dream.service.JrnlDreamService;
import io.nicheblog.dreamdiary.domain.jrnl.entry.model.JrnlEntryDto;
import io.nicheblog.dreamdiary.domain.jrnl.entry.service.JrnlEntryService;
import io.nicheblog.dreamdiary.extension.cache.event.CommentCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.cache.event.JrnlCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.cache.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CommentCacheEvictWorker
 * <pre>
 *  댓글 관련 캐시 제거 Worker
 * </pre>
 *
 * @author nichefish
 * @see EhCacheEvictEventListner
 **/
@Component
@RequiredArgsConstructor
@Log4j2
public class CommentCacheEvictWorker {

    private final JrnlDayService jrnlDayService;
    private final JrnlEntryService jrnlEntryService;
    private final JrnlDiaryService jrnlDiaryService;
    private final JrnlDreamService jrnlDreamService;
    private final ApplicationEventPublisherWrapper publisher;

    /**
     * 캐시 evict 처리
     *
     * @param event TagProcEvent
     */
    @Transactional
    public void handle(final CommentCacheEvictEvent event) throws Exception {
       final ContentType refContentType = event.getRefContentType();

        switch (refContentType) {
            case JRNL_DAY: {
                final JrnlDayDto jrnlDayDto = jrnlDayService.getDtlDto(event.getRefPostNo());
                final JrnlCacheEvictParam param = JrnlCacheEvictParam.of(jrnlDayDto);
                publisher.publishCustomEvent(new JrnlCacheEvictEvent(this, param, ContentType.JRNL_DAY));
                return;
            }
            case JRNL_ENTRY: {
                final JrnlEntryDto jrnlEntryDto = jrnlEntryService.getDtlDto(event.getRefPostNo());
                final JrnlCacheEvictParam param = JrnlCacheEvictParam.of(jrnlEntryDto);
                publisher.publishCustomEvent(new JrnlCacheEvictEvent(this, param, ContentType.JRNL_ENTRY));
                return;
            }
            case JRNL_DIARY:  {
                final JrnlDiaryDto jrnlDiaryDto = jrnlDiaryService.getDtlDto(event.getRefPostNo());
                final JrnlCacheEvictParam param = JrnlCacheEvictParam.of(jrnlDiaryDto);
                publisher.publishCustomEvent(new JrnlCacheEvictEvent(this, param, ContentType.JRNL_DIARY));
                return;
            }
            case JRNL_DREAM: {
                final JrnlDreamDto jrnlDreamDto = jrnlDreamService.getDtlDto(event.getRefPostNo());
                final JrnlCacheEvictParam param = JrnlCacheEvictParam.of(jrnlDreamDto);
                publisher.publishCustomEvent(new JrnlCacheEvictEvent(this, param, ContentType.JRNL_DREAM));
                return;
            }
        }
        log.warn("No CacheEvictor for comment found for ContentType: {}", refContentType);
    }
}
