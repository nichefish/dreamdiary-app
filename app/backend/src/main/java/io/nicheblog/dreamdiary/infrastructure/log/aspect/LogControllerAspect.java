package io.nicheblog.dreamdiary.infrastructure.log.aspect;

import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.infrastructure.log.event.LogEvent;
import io.nicheblog.dreamdiary.infrastructure.log.model.LogParam;
import io.nicheblog.dreamdiary.infrastructure.log.support.HttpRequestLogParamBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * {@link org.springframework.web.bind.annotation.RestController} 활동 로그 — LogParam 인자 없이
 * 응답 본문({@link io.nicheblog.dreamdiary.global.model.ServiceResponse}) 기준으로 적재한다.
 *
 * @see io.nicheblog.dreamdiary.infrastructure.log.interceptor.LogInterceptor
 */
@Aspect
@Component
@RequiredArgsConstructor
@Log4j2
public class LogControllerAspect {

    private final ApplicationEventPublisherWrapper publisher;

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void restController() {}

    @Around("restController() && execution(public * *(..))")
    public Object aroundRestController(final ProceedingJoinPoint joinPoint) throws Throwable {
        final Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        if (method.getDeclaringClass() == Object.class) {
            return joinPoint.proceed();
        }
        if (!AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class)) {
            return joinPoint.proceed();
        }

        final long start = System.currentTimeMillis();
        Object result = null;
        Throwable error = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (final Throwable t) {
            error = t;
            throw t;
        } finally {
            try {
                publishRestActivityLog(joinPoint, method, start, result, error);
            } catch (final Exception logEx) {
                log.error("REST activity log failed: {}", logEx.getMessage(), logEx);
            }
        }
    }

    private void publishRestActivityLog(
            final ProceedingJoinPoint joinPoint,
            final Method method,
            final long start,
            final Object result,
            final Throwable error
    ) {
        final ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return;
        }
        final HttpServletRequest request = attrs.getRequest();
        final HttpServletResponse response = attrs.getResponse();
        final long duration = System.currentTimeMillis() - start;
        final HandlerMethod handlerMethod = new HandlerMethod(joinPoint.getTarget(), method);
        final LogParam param = HttpRequestLogParamBuilder.build(request, response, handlerMethod, error, duration, result);
        request.setAttribute(HttpRequestLogParamBuilder.REST_ACTIVITY_LOGGED, Boolean.TRUE);
        publisher.publishAsyncEvent(new LogEvent(this, param));
    }
}
