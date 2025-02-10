package io.nicheblog.dreamdiary.extension.log.actvty.aspect;

import io.nicheblog.dreamdiary.extension.log.actvty.event.LogActvtyEvent;
import io.nicheblog.dreamdiary.extension.log.actvty.handler.LogActvtyEventListener;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.extension.log.actvty.aspect.helper.LogActvtyAspectHelper;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.ApplicationEventPublisher;
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
public class LogActvtyPageControllerAspect {

    private final ApplicationEventPublisher publisher;

    /**
     * Pointcut :: RestController(API)를 대상으로 지정합니다.
     */
    @Pointcut("within(@org.springframework.stereotype.Controller *)")
    public void pageControllerMethods() { }

    /**
     * Controller (Page) 메소드 성공시 응답 객체를 기반으로 로그를 기록합니다.
     *
     * @param joinPoint 메소드 이름, 파라미터, 호출된 클래스, 타겟 객체 등의 메타 정보를 담은 객체
     * @param result 사용자에게 반환된 view 경로 객체
     * TODO: 추가적인 인자들을 전달받아야 한다.
     * @see LogActvtyEventListener
     */
    @AfterReturning(pointcut = "pageControllerMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        // API 요청에 대한 결과값 로그 남기기
        log.info("Page Method {} completed. Result: {}", joinPoint.getSignature().getName(), result);

        final LogActvtyParam logParam = LogActvtyAspectHelper.extractLogParam(joinPoint);
        if (logParam == null) return;
        publisher.publishEvent(new LogActvtyEvent(this, logParam));
    }

    /**
     * Controller (Page) 메소드 실행 중 예외 발생시 에러를 기반으로 로그를 기록합니다.
     *
     * @param joinPoint 메소드 이름, 파라미터, 호출된 클래스, 타겟 객체 등의 메타 정보를 담은 객체
     * @param ex 익셉션 또는 에러
     * TODO: 추가적인 인자들을 전달받아야 한다.
     * @see LogActvtyEventListener
     */
    @AfterThrowing(pointcut = "pageControllerMethods()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        log.error("Page Method {} threw an exception: {}", joinPoint.getSignature().getName(), ex.getMessage());

        LogActvtyParam logParam = LogActvtyAspectHelper.extractLogParam(joinPoint);
        if (logParam == null) logParam = new LogActvtyParam();
        // 실패 여부, 에러 메시지, 활동 카테고리 설정
        logParam.setResult(false, MessageUtils.getExceptionMsg(ex));
        logParam.setExceptionInfo(ex);
        // logParam.setMethodName(joinPoint.getSignature().getName());
        // TODO: 컨트롤러의 getCtgr() 을 빼올 수 있는지?

        // 로그 이벤트 발행
        publisher.publishEvent(new LogActvtyEvent(this, logParam));
    }
}
