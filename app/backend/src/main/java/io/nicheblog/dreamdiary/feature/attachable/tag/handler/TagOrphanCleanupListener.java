package io.nicheblog.dreamdiary.feature.attachable.tag.handler;

import io.nicheblog.dreamdiary.feature.attachable.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 태그-컨텐츠 제거 이벤트를 수신해 고아 마스터 태그를 비동기로 정리한다.
 * 트랜잭션 커밋 후 실행되므로 등록/수정 응답 시간에 영향을 주지 않는다.
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class TagOrphanCleanupListener {

    private final TagService tagService;

    /**
     * 트랜잭션 커밋 후 비동기로 고아 태그를 삭제한다.
     *
     * @param event 제거된 태그 ID 목록을 담은 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTagContentRemoved(final TagContentRemovedEvent event) {
        try {
            tagService.deleteOrphansByIds(event.getRemovedTagIds());
        } catch (final Exception e) {
            log.warn("Async orphan tag cleanup failed: {}", e.getMessage());
        }
    }
}
