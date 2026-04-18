package io.nicheblog.dreamdiary.infrastructure.log.aspect.helper;

import io.nicheblog.dreamdiary.infrastructure.log.model.LogParam;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;

import java.lang.reflect.Field;

/**
 * 로그 Aspect 공통.
 */
@Log4j2
@UtilityClass
public class LogAspectHelper {

    public static LogParam extractLogParam(final JoinPoint joinPoint) {
        final Object[] args = joinPoint.getArgs();
        for (final Object arg : args) {
            if (arg instanceof LogParam) {
                LogParam logParam = (LogParam) arg;
                if (logParam.getActvtyCtgr() == null) logParam.setActvtyCtgr(LogAspectHelper.getActvtyCtgr(joinPoint.getTarget()));
                return logParam;
            }
        }

        return null;
    }

    public static ActvtyCtgr getActvtyCtgr(final Object target) {
        try {
            final Field field = target.getClass().getDeclaredField("actvtyCtgr");
            field.setAccessible(true);
            return (ActvtyCtgr) field.get(target);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            log.warn("Failed to get ActvtyCtgr from controller: {}", e.getMessage());
            return ActvtyCtgr.DEFAULT;
        }
    }
}
