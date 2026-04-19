package io.nicheblog.dreamdiary.feature.attachable.history.event;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.attachable.history.HistoryType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Getter
public class HistoryAddEvent
        extends ApplicationEvent {

    private final SecurityContext securityContext;
    private final BaseAttachableEntity entitySnapshot;
    private final BaseAttachableEntity afterEntity;
    private final HistoryType historyType;
    private final Integer fromHistoryId;

    public HistoryAddEvent(final Object source, final BaseAttachableEntity entitySnapshot, final BaseAttachableEntity afterEntity) {
        this(source, entitySnapshot, afterEntity, HistoryType.CHANGE, null);
    }

    public HistoryAddEvent(
            final Object source,
            final BaseAttachableEntity entitySnapshot,
            final BaseAttachableEntity afterEntity,
            final HistoryType historyType,
            final Integer fromHistoryId
    ) {
        super(source);
        this.securityContext = SecurityContextHolder.getContext();
        this.entitySnapshot = entitySnapshot;
        this.afterEntity = afterEntity;
        this.historyType = historyType != null ? historyType : HistoryType.CHANGE;
        this.fromHistoryId = fromHistoryId;
    }
}
