package io.nicheblog.dreamdiary.global._common._clsf.tag.handler;

import io.nicheblog.dreamdiary.global._common._clsf.tag.event.TagProcEvent;
import io.nicheblog.dreamdiary.global._common._clsf.tag.service.ContentTagService;
import io.nicheblog.dreamdiary.global._common._clsf.tag.service.TagService;
import io.nicheblog.dreamdiary.global._common.cache.event.EhCacheEvictEvent;
import io.nicheblog.dreamdiary.global._common.cache.handler.EhCacheEvictEventListner;
import io.nicheblog.dreamdiary.global.config.AsyncConfig;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * TagEventListener
 * <pre>
 *  태그 관련 이벤트 처리 핸들러.
 * </pre>
 *
 * @author nichefish
 * @see AsyncConfig
 */
@Component
@RequiredArgsConstructor
public class TagEventListener {

    private final TagService tagService;
    private final ContentTagService contentTagService;
    private final ApplicationEventPublisher publisher;

    /**
     * 태그 이벤트를 처리한다.
     * 삭제된 엔티티 재조회와 관련될 수 있으므로 별도 트랜잭션으로 처리.
     * 
     * @param event 처리할 이벤트 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     * @see EhCacheEvictEventListner
     */
    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleTagProcEvent(final TagProcEvent event) throws Exception {

        // 태그 객체 없이 키값만 넘어오면? 컨텐츠 삭제.
        boolean isContentDelete = (event.getTagCmpstn() == null);
        BaseClsfKey clsfKey = event.getClsfKey();
        if (isContentDelete) {
            // 기존 컨텐츠-태그 전부 삭제
            contentTagService.delExistingContentTags(clsfKey);
        } else {
            // 태그 처리
            tagService.procTags(clsfKey, event.getTagCmpstn());
        }
        // 관련 캐시 클리어
        publisher.publishEvent(new EhCacheEvictEvent(this, clsfKey.getPostNo(), clsfKey.getContentType()));
        // 태그 테이블 refresh (연관관계 없는 메인 태그 삭제)
        tagService.deleteNoRefTags();
    }
}
