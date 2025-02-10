package io.nicheblog.dreamdiary.extension.log.actvty.aspect.helper;

import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyParam;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;

import java.lang.reflect.Field;

/**
 * LogActvtyAspectUtils
 * <pre>
 *  로그 공통 처리 Aspect용 공통 로직 분리.
 * </pre>
 *
 * @author nichefish
 */
@Log4j2
public class LogActvtyAspectHelper {

    /**
     * 컨트롤러 메소드에서 LogActvtyParam를 추출합니다.
     *
     * @param joinPoint 메소드 이름, 파라미터, 호출된 클래스, 타겟 객체 등의 메타 정보를 담은 객체
     */
    public static LogActvtyParam extractLogParam(final JoinPoint joinPoint) {
        // JoinPoint에서 LogActvtyParam을 추출하는 로직
        final Object[] args = joinPoint.getArgs();
        for (final Object arg : args) {
            if (arg instanceof LogActvtyParam) {
                LogActvtyParam logParam = (LogActvtyParam) arg;
                // actvtyCtgr 값 자동 할당
                if (logParam.getActvtyCtgr() == null) logParam.setActvtyCtgr(LogActvtyAspectHelper.getActvtyCtgr(joinPoint.getTarget()));
                return logParam;
            }
        }

        return null;
    }

    /**
     * 컨트롤러 필드로 선언된 ActvtyCtgr 값을 가져옵니다.
     *
     * @param target 컨트롤러 객체
     * @return {@link ActvtyCtgr} -- 컨트롤러 필드로 선언된 ActvtyCtgr 값
     */
    public static ActvtyCtgr getActvtyCtgr(final Object target) {
        try {
            final Field field = target.getClass().getDeclaredField("actvtyCtgr");
            field.setAccessible(true);
            return (ActvtyCtgr) field.get(target);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            log.warn("Failed to get ActvtyCtgr from controller: {}", e.getMessage());
            return ActvtyCtgr.DEFAULT;  // 기본 값 반환
        }
    }
}
