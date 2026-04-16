package io.nicheblog.dreamdiary.feature.attachable.history.handler;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.attachable.history.event.HistoryAddEvent;
import io.nicheblog.dreamdiary.feature.attachable.history.service.HistoryChangeDetector;
import io.nicheblog.dreamdiary.feature.attachable.history.service.HistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Log4j2
public class HistoryEventListener {

    private final HistoryService historyService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleHistoryAddEvent(final HistoryAddEvent event) {
        log.debug("HistoryEventListener.handleHistoryAddEvent() - event : {}", event.toString());

        final var previousSecurityContext = SecurityContextHolder.getContext();
        try {
            SecurityContextHolder.setContext(event.getSecurityContext());

            if (!(event.getSource() instanceof HistoryChangeDetector<?> detector)) return;
            if (event.getAfterEntity() == null) return;

            final String content = this.resolveHistoryCn(detector, event);
            if (event.getEntitySnapshot() != null && StringUtils.isBlank(content)) return;

            historyService.addHistory(event.getAfterEntity().getAttachableKey(), content, event.getHistoryType(), event.getFromHistoryId());
        } finally {
            SecurityContextHolder.setContext(previousSecurityContext);
        }
    }

    @SuppressWarnings("unchecked")
    private String resolveHistoryCn(final HistoryChangeDetector<?> detector, final HistoryAddEvent event) {
        try {
            return ((HistoryChangeDetector<BaseAttachableEntity>) detector).resolveHistoryCn(event.getEntitySnapshot(), event.getAfterEntity());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
