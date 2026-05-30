package io.nicheblog.dreamdiary.feature.attachable.history.service;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;

public interface HistoryChangeDetector<Entity extends BaseAttachableEntity> {

    String resolveHistoryCn(final Entity beforeEntity, final Entity afterEntity) throws Exception;
}
