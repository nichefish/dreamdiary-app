package io.nicheblog.dreamdiary.feature.attachable.history.service;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Objects;

public final class HistoryChangeUtils {

    private HistoryChangeUtils() {
    }

    public static boolean hasCnChanged(final BaseAttachableEntity beforeEntity, final BaseAttachableEntity afterEntity) throws Exception {
        return !Objects.equals(
                StringUtils.defaultString(resolveCn(beforeEntity)),
                StringUtils.defaultString(resolveCn(afterEntity))
        );
    }

    public static String resolveSnapshotCn(final BaseAttachableEntity beforeEntity, final BaseAttachableEntity afterEntity) throws Exception {
        if (beforeEntity == null) return null;
        if (!hasCnChanged(beforeEntity, afterEntity)) return null;
        return StringUtils.defaultIfBlank(resolveCn(beforeEntity), null);
    }

    public static String resolveCn(final BaseAttachableEntity entity) throws Exception {
        if (entity == null) return null;

        final Method getter = entity.getClass().getMethod("getContent");
        final Object value = getter.invoke(entity);
        return value instanceof String str ? str : null;
    }
}
