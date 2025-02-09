package io.nicheblog.dreamdiary.extension.tag.handler;

import io.nicheblog.dreamdiary.extension.tag.event.TagProcEvent;
import io.nicheblog.dreamdiary.extension.tag.service.ContentTagService;
import io.nicheblog.dreamdiary.extension.tag.service.TagService;
import io.nicheblog.dreamdiary.global._common.cache.event.EhCacheEvictEvent;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * TagWorker
 * <pre>
 *  태그 처리 Worker :: Runnable 구현 (Queue 처리)
 *  Queue에서 TagEvent를 가져와 후속 처리를 한다.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class TagWorker
        implements Runnable {

    private final TagService tagService;
    private final ContentTagService contentTagService;
    private final ApplicationEventPublisher publisher;

    /** 태그 queue */
    private static final BlockingQueue<TagProcEvent> tagQueue = new LinkedBlockingQueue<>();

    @PostConstruct
    public void init() {
        final Thread workerThread = new Thread(this);
        workerThread.start();
    }

    /**
     * 태그 Queue에서 TagProcEvent를 가져와 처리한다.
     */
    @Override
    public void run() {
        try {
            while (true) {
                // Blocks until an element is available
                final TagProcEvent tagEvent = tagQueue.take();
                SecurityContextHolder.setContext(tagEvent.getSecurityContext());

                // 태그 객체 없이 키값만 넘어오면? 컨텐츠 삭제.
                // 🔥 이벤트 발생 당시의 SecurityContext 복원
                final boolean isContentDelete = (tagEvent.getTagCmpstn() == null);
                final BaseClsfKey clsfKey = tagEvent.getClsfKey();
                if (isContentDelete) {
                    // 기존 컨텐츠-태그 전부 삭제
                    contentTagService.delExistingContentTags(clsfKey);
                } else {
                    // 태그 처리
                    tagService.procTags(clsfKey, tagEvent.getTagCmpstn());
                }
                // 관련 캐시 클리어
                publisher.publishEvent(new EhCacheEvictEvent(this, clsfKey.getPostNo(), clsfKey.getContentType()));
                // 태그 테이블 refresh (연관관계 없는 메인 태그 삭제)
                tagService.deleteNoRefTags();
            }
        } catch (final InterruptedException e) {
            log.warn("tag handling failed", e);
            Thread.currentThread().interrupt();
        } catch (final Exception e) {
            log.warn("tag handling failed", e);
        }
    }

    /**
     * 태그 이벤트를 큐에 추가합니다.
     *
     * @param event 큐에 추가할 TagActvtyEvent / TagAnonActvtyEvent 객체
     */
    public void offer(final TagProcEvent event) {
        boolean isOffered = tagQueue.offer(event);
        if (!isOffered) log.warn("queue offer failed... {}", event.toString());
    }
}
