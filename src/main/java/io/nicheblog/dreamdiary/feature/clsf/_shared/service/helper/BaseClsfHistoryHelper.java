package io.nicheblog.dreamdiary.feature.clsf._shared.service.helper;

import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfEntity;
import io.nicheblog.dreamdiary.feature.clsf.history.HistoryType;
import io.nicheblog.dreamdiary.feature.clsf.history.entity.embed.HistoryEmbed;
import io.nicheblog.dreamdiary.feature.clsf.history.entity.embed.HistoryEmbedModule;
import io.nicheblog.dreamdiary.feature.clsf.history.event.HistoryAddEvent;
import io.nicheblog.dreamdiary.feature.clsf.history.service.HistoryChangeDetector;
import io.nicheblog.dreamdiary.feature.clsf.history.service.HistoryChangeUtils;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.handler.SpringBeanProvider;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class BaseClsfHistoryHelper {

    public static boolean isHistoryModule(final Object entity) {
        return entity instanceof HistoryEmbedModule;
    }

    public static void applyModifyHistory(final BaseClsfEntity beforeEntity, final BaseClsfEntity afterEntity) throws Exception {
        if (!isHistoryModule(afterEntity)) return;
        if (!HistoryChangeUtils.hasCnChanged(beforeEntity, afterEntity)) return;

        ((HistoryEmbedModule) afterEntity).setHistory(new HistoryEmbed(true));
    }

    public static <Entity extends BaseClsfEntity> void publishHistoryEventIfSupported(
            final Object source,
            final Entity beforeEntity,
            final Entity updatedEntity
    ) {
        publishHistoryEventIfSupported(source, beforeEntity, updatedEntity, HistoryType.CHANGE, null);
    }

    public static <Entity extends BaseClsfEntity> void publishHistoryEventIfSupported(
            final Object source,
            final Entity beforeEntity,
            final Entity updatedEntity,
        final HistoryType historyType,
        final Integer fromHistoryNo
    ) {
        if (!isHistoryModule(updatedEntity)) return;

        final ApplicationEventPublisherWrapper publisher = SpringBeanProvider.getBean(ApplicationEventPublisherWrapper.class);

        publisher.publishEvent(new HistoryAddEvent(
                (HistoryChangeDetector<Entity>) HistoryChangeUtils::resolveSnapshotCn,
                beforeEntity,
                updatedEntity,
                historyType,
                fromHistoryNo
        ));
    }

}
