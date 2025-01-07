package io.nicheblog.dreamdiary.global.aspect.log;

import io.nicheblog.dreamdiary.global._common.log.actvty.event.LogActvtyEvent;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * LogActvtyRestControllerAspect
 * <pre>
 *  RestController에서의 로그 공통 처리 Aspect.
 * </pre>
 *
 * @author nichefish
 */
@Aspect
@Component
@RequiredArgsConstructor
@Log4j2
public class LogActvtyRestControllerAspect {

    private final ApplicationEventPublisher publisher;

    /**
     * Pointcut :: RestController(API) 중 *Ajax로 끝나는 메소드를 대상으로 지정한다.
     */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *) && execution(* *Ajax(..))")
    public void apiControllerMethods() { }

    /**
     * RestController (API) *Ajax 메소드 성공시 응답 객체를 기반으로 로그를 기록합니다.
     *
     * @param joinPoint 메소드 이름, 파라미터, 호출된 클래스, 타겟 객체 등의 메타 정보를 담은 객체
     * @param result 사용자에게 반환된 {@link ResponseEntity} 객체
     * TODO: 추가적인 인자들을 전달받아야 한다.
     */
    // @Around("apiControllerMethods()")
    @AfterReturning(pointcut = "apiControllerMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        // API 요청에 대한 결과값 로그 남기기
        log.info("API Method {} completed. Result: {}", joinPoint.getSignature().getName(), result);

        final LogActvtyParam logParam = LogActvtyAspectHelper.extractLogParam(joinPoint);
        if (logParam == null) return;

        publisher.publishEvent(new LogActvtyEvent(this, logParam));
    }

    /**
     * RestController (API) *Ajax 메소드 실행 중 예외 발생시 에러를 기반으로 로그를 기록합니다.
     *
     * @param joinPoint 메소드 이름, 파라미터, 호출된 클래스, 타겟 객체 등의 메타 정보를 담은 객체
     * @param ex 익셉션 또는 에러
     * TODO: 추가적인 인자들을 전달받아야 한다.
     */
    @AfterThrowing(pointcut = "apiControllerMethods()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        log.error("Ajax Method {} threw an exception: {}", joinPoint.getSignature().getName(), ex.getMessage());

        LogActvtyParam logParam = LogActvtyAspectHelper.extractLogParam(joinPoint);
        if (logParam == null) logParam = new LogActvtyParam();
        // 실패 여부, 에러 메시지, 활동 카테고리 설정
        logParam.setResult(false, MessageUtils.getExceptionMsg(ex));
        logParam.setExceptionInfo(ex);
        // logParam.setMethodName(joinPoint.getSignature().getName());

        // 로그 이벤트 발행
         publisher.publishEvent(new LogActvtyEvent(this, logParam));
    }
}
