package io.nicheblog.dreamdiary.infrastructure.log.actvty.aspect;

import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * LogActvtyPageControllerAspect
 * <pre>
 *  페이지 조회 Controller에서의 로그 공통 처리 Aspect.
 * </pre>
 *
 * @author nichefish
 */
@Aspect
@Component
@RequiredArgsConstructor
@Log4j2
public class LogActvtyControllerAspect {

    private final ApplicationEventPublisherWrapper publisher;

    @Pointcut("@within(org.springframework.stereotype.Controller)")
    public void allControllers() {}

    /**
     * Controller (API) 메소드 성공시 응답 객체를 기반으로 로그를 기록합니다.
     *
     * @param joinPoint 메소드 이름, 파라미터, 호출된 클래스, 타겟 객체 등의 메타 정보를 담은 객체
     */
    @Around("allControllers() && @annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public Object around(final ProceedingJoinPoint joinPoint) throws Throwable {
        // 시작 시간 고정
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
            // 소요 시간 산정
            final long duration = System.currentTimeMillis() - start;
            try {
                // final LogActvtyParam logParam = this.createLogParam(joinPoint, result, error, duration);
                // if (logParam != null) publisher.publishAsyncEvent(new LogActvtyEvent(this, logParam));
            } catch (Exception logEx) {
                log.error("log insert failed: {}", logEx.getMessage(), logEx);
            }
        }
    }
}
