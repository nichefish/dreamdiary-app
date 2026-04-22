package io.nicheblog.dreamdiary.infrastructure.log.support;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.infrastructure.log.model.LogParam;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.log.type.LogType;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.experimental.UtilityClass;
import org.slf4j.MDC;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * HTTP 요청 단위 {@link LogParam} 조립 (인터셉터·AOP 공통).
 */
@UtilityClass
public class HttpRequestLogParamBuilder {

    /**
     * {@link RestController}에서 AOP가 활동 로그를 남긴 뒤 표시 — {@link io.nicheblog.dreamdiary.infrastructure.log.interceptor.LogInterceptor}가 중복 발행하지 않도록 한다.
     */
    public static final String REST_ACTIVITY_LOGGED = HttpRequestLogParamBuilder.class.getName() + ".REST_ACTIVITY_LOGGED";

    public static LogParam build(
            final HttpServletRequest request,
            final @Nullable HttpServletResponse response,
            final HandlerMethod handlerMethod,
            final @Nullable Throwable ex,
            final long durationMs,
            final @Nullable Object returnValue
    ) {
        final LogParam param = new LogParam();

        param.setTraceId(MDC.get("traceId"));
        param.setRequestUri(request.getRequestURI());
        param.setHttpMethod(request.getMethod());
        param.setUsername(AuthUtils.getLoginUsername());
        param.setDurationMs(durationMs);
        param.setReferer(request.getHeader(Constant.REFERER));
        param.setIpAddr(AuthUtils.getRemoteIpAddr());

        final int status = response != null ? response.getStatus() : 200;
        param.setHttpStatus(status);

        final boolean success = (ex == null) && status < 400;
        param.setRslt(success);

        if (ex != null) {
            param.setExceptionInfo(ex);
        }

        final Object bean = handlerMethod.getBean();
        if (bean instanceof BaseControllerImpl baseController) {
            param.setActvtyCtgr(baseController.getActvtyCtgr());
        } else {
            param.setActvtyCtgr(resolveActvtyCtgr(bean));
        }

        final Class<?> controllerClass = handlerMethod.getBeanType();
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

        param.setSignature(handlerMethod.getBeanType().getSimpleName() + "." + handlerMethod.getMethod().getName());

        applyReturnValue(param, returnValue);

        return param;
    }

    /**
     * {@link ResponseEntity} 본문이 {@link ServiceResponse}이면 비즈니스 성공·메시지를 로그에 반영한다.
     */
    public static void applyReturnValue(final LogParam param, final @Nullable Object returnValue) {
        if (!(returnValue instanceof ResponseEntity<?> re)) {
            return;
        }
        param.setHttpStatus(re.getStatusCode().value());
        final Object body = re.getBody();
        if (body instanceof ServiceResponse sr) {
            if (sr.getRslt() != null) {
                param.setRslt(sr.getRslt());
            }
            if (sr.getMessage() != null) {
                param.setRsltMsg(sr.getMessage());
            }
        }
    }

    private static ActvtyCtgr resolveActvtyCtgr(final Object target) {
        try {
            final Method getter = target.getClass().getMethod("getActvtyCtgr");
            return (ActvtyCtgr) getter.invoke(target);
        } catch (final ReflectiveOperationException ignored) {
            // fall through
        }
        try {
            final var field = target.getClass().getDeclaredField("actvtyCtgr");
            field.setAccessible(true);
            return (ActvtyCtgr) field.get(target);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            return ActvtyCtgr.DEFAULT;
        }
    }
}
