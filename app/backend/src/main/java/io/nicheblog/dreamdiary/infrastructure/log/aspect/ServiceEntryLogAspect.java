package io.nicheblog.dreamdiary.infrastructure.log.aspect;

import io.nicheblog.dreamdiary.infrastructure.log.annotation.NoServiceEntryLog;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

/**
 * {@code @Service} 빈의 공개 메서드 진입(및 선택적 완료)을 한 줄로 남긴다.
 * <p>인자 값은 기록하지 않는다. {@code app.logging.service-entry.enabled} 로 켠다.</p>
 *
 * @see NoServiceEntryLog
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 200)
@Log4j2
public class ServiceEntryLogAspect {

    @Value("${app.logging.service-entry.enabled:false}")
    private boolean enabled;

    @Value("${app.logging.service-entry.log-finish:false}")
    private boolean logFinish;

    @Pointcut("@within(org.springframework.stereotype.Service) && execution(public * io.nicheblog.dreamdiary..*(..))")
    public void servicePublicMethods() {}

    @Around("servicePublicMethods()")
    public Object aroundService(final ProceedingJoinPoint joinPoint) throws Throwable {
        if (!enabled) {
            return joinPoint.proceed();
        }
        final Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        final Object target = joinPoint.getTarget();
        if (shouldSkip(method, target)) {
            return joinPoint.proceed();
        }

        final String bean = ClassUtils.getUserClass(target).getSimpleName();
        final String methodName = method.getName();
        final int argCount = joinPoint.getArgs() != null ? joinPoint.getArgs().length : 0;

        log.info("SVC_TX phase=start bean={} method={} argCount={}", bean, methodName, argCount);
        final long t0 = System.currentTimeMillis();
        try {
            final Object result = joinPoint.proceed();
            if (logFinish) {
                log.info(
                        "SVC_TX phase=done bean={} method={} durationMs={}",
                        bean,
                        methodName,
                        System.currentTimeMillis() - t0
                );
            }
            return result;
        } catch (final Throwable t) {
            log.warn(
                    "SVC_TX phase=fail bean={} method={} durationMs={} ex={}",
                    bean,
                    methodName,
                    System.currentTimeMillis() - t0,
                    t.getClass().getSimpleName(),
                    t
            );
            throw t;
        }
    }

    private static boolean shouldSkip(final Method method, final Object target) {
        if (method.getDeclaringClass() == Object.class) {
            return true;
        }
        final String name = method.getName();
        if ("equals".equals(name) || "hashCode".equals(name) || "toString".equals(name)) {
            return true;
        }
        final Class<?> userClass = ClassUtils.getUserClass(target);
        if (AnnotatedElementUtils.hasAnnotation(method, NoServiceEntryLog.class)) {
            return true;
        }
        return AnnotatedElementUtils.hasAnnotation(userClass, NoServiceEntryLog.class);
    }
}
