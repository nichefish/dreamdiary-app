package io.nicheblog.dreamdiary.auth.security.interceptor;

import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.config.WebMvcContextConfig;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * CsrfInterceptor
 * <pre>
 *  CSRF 처리 인터셉터
 * </pre>
 *
 * @author nichefish
 * @see WebMvcContextConfig
 */
@Component
@Log4j2
public class CsrfInterceptor
        implements HandlerInterceptor {

    /**
     * preHandle: 요청 처리 전 controller로 들어가기 전에 작동한다.
     * true = 통과, false = 미통과
     */
    @Override
    public boolean preHandle(
            final HttpServletRequest request,
            final @NotNull HttpServletResponse response,
            final @NotNull Object handler
    ) {

        final String domain = request.getServerName();
        final String refererValue = request.getHeader(Constant.REFERER);
        final boolean hasReferer = StringUtils.isNotEmpty(refererValue);
        if (hasReferer) return refererValue.contains(domain);

        return true;
    }
}
