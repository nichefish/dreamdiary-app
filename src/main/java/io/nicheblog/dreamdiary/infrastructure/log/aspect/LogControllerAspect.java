package io.nicheblog.dreamdiary.infrastructure.log.aspect;

import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 페이지 조회 Controller 로그 공통 처리 (확장용).
 */
@Aspect
@Component
@RequiredArgsConstructor
@Log4j2
public class LogControllerAspect {

    private final ApplicationEventPublisherWrapper publisher;

    @Pointcut("@within(org.springframework.stereotype.Controller)")
    public void allControllers() {}

    @Around("allControllers() && @annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public Object around(final ProceedingJoinPoint joinPoint) throws Throwable {
        final long start = System.currentTimeMillis();
        Object result = null;
        Exception error = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            error = e;
            throw e;
        } finally {
            final long duration = System.currentTimeMillis() - start;
            try {
                // final LogParam logParam = this.createLogParam(joinPoint, result, error, duration);
                // if (logParam != null) publisher.publishAsyncEvent(new LogEvent(this, logParam));
            } catch (Exception logEx) {
                log.error("log insert failed: {}", logEx.getMessage(), logEx);
            }
        }
    }
}
