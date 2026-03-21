package io.nicheblog.dreamdiary.infrastructure.log.actvty.interceptor;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.Constant;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.LogType;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.event.LogActvtyEvent;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.model.LogActvtyParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HandlerInterceptor
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class LogActvtyInterceptor implements HandlerInterceptor {

    private final ApplicationEventPublisherWrapper publisher;

    /**
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler chosen handler to execute, for type and/or instance evaluation
     * @return true
     */
    @Override
    public boolean preHandle(final HttpServletRequest request, final @NotNull HttpServletResponse response, final @NotNull Object handler) throws Exception {
        // 시작 시간 저장
        request.setAttribute("startTime", System.currentTimeMillis());
        // 반드시 true
        return true;
    }

    /**
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler the handler (or {@link HandlerMethod}) that started asynchronous execution
     * @param ex LogActvtyParam
     */
    @Override
    public void afterCompletion(final HttpServletRequest request, final @NotNull HttpServletResponse response, final @NotNull Object handler, final Exception ex) {

        final Long start = (Long) request.getAttribute("startTime");
        if (start == null) return;
        final long duration = System.currentTimeMillis() - start;

        if (!(handler instanceof HandlerMethod handlerMethod)) return;

        final String traceId = MDC.get("traceId");
        log.info("ACCESS traceId={} method={} url={} status={} duration={} user={}",  traceId,  request.getMethod(), request.getRequestURI(), response.getStatus(), duration, AuthUtils.getLgnUserId());

        final LogActvtyParam param = createLogParam(request, response, handlerMethod, ex, duration);
        if (param != null) {
            publisher.publishAsyncEvent(new LogActvtyEvent(this, param));
        }
    }

    /**
     * 로그 파라미터 생성
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param handler Object
     * @param ex Exception
     * @param duration long
     * @return LogActvtyParam
     */
    private LogActvtyParam createLogParam(final HttpServletRequest request, final HttpServletResponse response, final Object handler, final Exception ex, final long duration) {

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return null; // 정적 리소스 등 제외
        }

        final LogActvtyParam param = new LogActvtyParam();

        // ===== 기본 정보 =====
        param.setTraceId(MDC.get("traceId"));
        param.setRequestUri(request.getRequestURI());
        param.setHttpMethod(request.getMethod());
        param.setUserId(AuthUtils.getLgnUserId());
        param.setDurationMs(duration);
        param.setHttpStatus(response.getStatus());
        param.setReferer(request.getHeader(Constant.REFERER));
        param.setIpAddr(AuthUtils.getAcsIpAddr());       // 작업 IP

        // ===== 성공 여부 판단 =====
        final boolean success = (ex == null) && response.getStatus() < 400;
        param.setRslt(success);

        // ===== 예외 정보 =====
        if (ex != null) {
            param.setExceptionInfo(ex);
        }

        // ===== 컨트롤러 타입 구분 =====
        final Class<?> controllerClass = handlerMethod.getBeanType();
        final Object bean = handlerMethod.getBean();

        if (bean instanceof BaseControllerImpl baseController) {
            final ActvtyCtgr ctgr = baseController.getActvtyCtgr();
            param.setActvtyCtgr(ctgr);
        }

        final boolean isRest = AnnotatedElementUtils.hasAnnotation(controllerClass, RestController.class);
        if (isRest) {
            // REST는 HTTP Method 기반 분류
            final String method = request.getMethod();
            if ("GET".equalsIgnoreCase(method)) {
                param.setLogType(LogType.VIEW);
            } else {
                param.setLogType(LogType.ACTION);
            }
        } else {
            param.setLogType(LogType.PAGE);
        }

        // ===== 컨트롤러 메소드 정보 =====
        final String signature = controllerClass.getSimpleName() + "." + handlerMethod.getMethod().getName();
        param.setSignature(signature);

        return param;
    }
}
