package io.nicheblog.dreamdiary.feature.clsf.history.service;

import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfEntity;

public interface HistoryChangeDetector<Entity extends BaseClsfEntity> {

    String resolveHistoryCn(final Entity beforeEntity, final Entity afterEntity) throws Exception;
}
