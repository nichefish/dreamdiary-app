package io.nicheblog.dreamdiary.feature.clsf.history.event;

import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfEntity;
import io.nicheblog.dreamdiary.feature.clsf.history.HistoryType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Getter
public class HistoryAddEvent
        extends ApplicationEvent {

    private final SecurityContext securityContext;
    private final BaseClsfEntity entitySnapshot;
    private final BaseClsfEntity afterEntity;
    private final HistoryType historyType;
    private final Integer fromHistoryNo;

    public HistoryAddEvent(final Object source, final BaseClsfEntity entitySnapshot, final BaseClsfEntity afterEntity) {
        this(source, entitySnapshot, afterEntity, HistoryType.CHANGE, null);
    }

    public HistoryAddEvent(
            final Object source,
            final BaseClsfEntity entitySnapshot,
            final BaseClsfEntity afterEntity,
            final HistoryType historyType,
            final Integer fromHistoryNo
    ) {
        super(source);
        this.securityContext = SecurityContextHolder.getContext();
        this.entitySnapshot = entitySnapshot;
        this.afterEntity = afterEntity;
        this.historyType = historyType != null ? historyType : HistoryType.CHANGE;
        this.fromHistoryNo = fromHistoryNo;
    }
}
