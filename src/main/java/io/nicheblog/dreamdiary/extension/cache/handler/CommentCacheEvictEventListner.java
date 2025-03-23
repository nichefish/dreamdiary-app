package io.nicheblog.dreamdiary.extension.cache.handler;

import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.domain.jrnl.day.service.JrnlDayService;
import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.domain.jrnl.diary.service.JrnlDiaryService;
import io.nicheblog.dreamdiary.domain.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.domain.jrnl.dream.service.JrnlDreamService;
import io.nicheblog.dreamdiary.extension.cache.event.CommentCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.cache.event.JrnlCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.cache.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * CommentCacheEvictEventListner
 * <pre>
 *  댓글 관련 캐시 제거 이벤트 처리 핸들러.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class CommentCacheEvictEventListner {

    private final JrnlDayService jrnlDayService;
    private final JrnlDiaryService jrnlDiaryService;
    private final JrnlDreamService jrnlDreamService;
    private final ApplicationEventPublisherWrapper publisher;

    /**
     * 댓글 관련 캐시 제거 이벤트를 처리한다.
     *
     * @param event 처리할 이벤트 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @EventListener
    public void handleJrnlCacheEvictEvent(final CommentCacheEvictEvent event) throws Exception {
        final ContentType refContentType = event.getRefContentType();

        switch (refContentType) {
            case JRNL_DAY: {
                JrnlDayDto jrnlDayDto = jrnlDayService.getDtlDto(event.getRefPostNo());
                JrnlCacheEvictParam param = JrnlCacheEvictParam.of(jrnlDayDto);
                publisher.publishEvent(new JrnlCacheEvictEvent(this, param, ContentType.JRNL_DAY));
                break;
            }
            case JRNL_DIARY:  {
                JrnlDiaryDto jrnlDiaryDto = jrnlDiaryService.getDtlDto(event.getRefPostNo());
                JrnlCacheEvictParam param = JrnlCacheEvictParam.of(jrnlDiaryDto);
                publisher.publishEvent(new JrnlCacheEvictEvent(this, param, ContentType.JRNL_DIARY));
                break;
            }
            case JRNL_DREAM: {
                JrnlDreamDto jrnlDreamDto = jrnlDreamService.getDtlDto(event.getRefPostNo());
                JrnlCacheEvictParam param = JrnlCacheEvictParam.of(jrnlDreamDto);
                publisher.publishEvent(new JrnlCacheEvictEvent(this, param, ContentType.JRNL_DREAM));
                break;
            }
        }
        log.warn("No CacheEvictor for comment found for ContentType: {}", refContentType);
    }
}
