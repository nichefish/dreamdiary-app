package io.nicheblog.dreamdiary.infrastructure.log.interceptor;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.infrastructure.log.event.LogEvent;
import io.nicheblog.dreamdiary.infrastructure.log.model.LogParam;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.log.type.LogType;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
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
 * 요청 단위 로그 파라미터 수집.
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class LogInterceptor implements HandlerInterceptor {

    private final ApplicationEventPublisherWrapper publisher;

    @Override
    public boolean preHandle(final HttpServletRequest request, final @NotNull HttpServletResponse response, final @NotNull Object handler) throws Exception {
        request.setAttribute("startTime", System.currentTimeMillis());
        if (handler instanceof HandlerMethod handlerMethod) {
            log.info(
                    "REQUEST_START method={} uri={} handler={} user={} ip={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    getHandlerSignature(handlerMethod),
                    getUsernameForLog(),
                    AuthUtils.getRemoteIpAddr()
            );
        }
        return true;
    }

    @Override
    public void afterCompletion(final HttpServletRequest request, final @NotNull HttpServletResponse response, final @NotNull Object handler, final Exception ex) {

        final Long start = (Long) request.getAttribute("startTime");
        if (start == null) return;
        final long duration = System.currentTimeMillis() - start;

        if (!(handler instanceof HandlerMethod handlerMethod)) return;

        final String handlerSignature = getHandlerSignature(handlerMethod);
        final String username = getUsernameForLog();

        if (ex != null) {
            log.error(
                    "REQUEST_FAILED method={} uri={} handler={} status={} durationMs={} user={} ip={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    handlerSignature,
                    response.getStatus(),
                    duration,
                    username,
                    AuthUtils.getRemoteIpAddr(),
                    ex
            );
        } else if (response.getStatus() >= 400) {
            log.warn(
                    "REQUEST_END method={} uri={} handler={} status={} durationMs={} user={} ip={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    handlerSignature,
                    response.getStatus(),
                    duration,
                    username,
                    AuthUtils.getRemoteIpAddr()
            );
        } else {
            log.info(
                    "REQUEST_END method={} uri={} handler={} status={} durationMs={} user={} ip={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    handlerSignature,
                    response.getStatus(),
                    duration,
                    username,
                    AuthUtils.getRemoteIpAddr()
            );
        }

        final LogParam param = createLogParam(request, response, handlerMethod, ex, duration);
        if (param != null) {
            publisher.publishAsyncEvent(new LogEvent(this, param));
        }
    }

    private LogParam createLogParam(final HttpServletRequest request, final HttpServletResponse response, final Object handler, final Exception ex, final long duration) {

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return null;
        }

        final LogParam param = new LogParam();

        param.setTraceId(MDC.get("traceId"));
        param.setRequestUri(request.getRequestURI());
        param.setHttpMethod(request.getMethod());
        param.setUsername(AuthUtils.getLoginUsername());
        param.setDurationMs(duration);
        param.setHttpStatus(response.getStatus());
        param.setReferer(request.getHeader(Constant.REFERER));
        param.setIpAddr(AuthUtils.getRemoteIpAddr());

        final boolean success = (ex == null) && response.getStatus() < 400;
        param.setRslt(success);

        if (ex != null) {
            param.setExceptionInfo(ex);
        }

        final Class<?> controllerClass = handlerMethod.getBeanType();
        final Object bean = handlerMethod.getBean();

        if (bean instanceof BaseControllerImpl baseController) {
            final ActvtyCtgr ctgr = baseController.getActvtyCtgr();
            param.setActvtyCtgr(ctgr);
        }

        final boolean isRest = AnnotatedElementUtils.hasAnnotation(controllerClass, RestController.class);
        if (isRest) {
            final String method = request.getMethod();
            if ("GET".equalsIgnoreCase(method)) {
                param.setLogType(LogType.VIEW);
            } else {
                param.setLogType(LogType.ACTION);
            }
        } else {
            param.setLogType(LogType.PAGE);
        }

        param.setSignature(getHandlerSignature(handlerMethod));

        return param;
    }

    private String getHandlerSignature(final HandlerMethod handlerMethod) {
        return handlerMethod.getBeanType().getSimpleName() + "." + handlerMethod.getMethod().getName();
    }

    private String getUsernameForLog() {
        final String username = AuthUtils.getLoginUsername();
        return username == null ? "anonymous" : username;
    }
}
