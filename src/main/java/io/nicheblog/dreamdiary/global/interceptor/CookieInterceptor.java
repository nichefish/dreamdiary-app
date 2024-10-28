package io.nicheblog.dreamdiary.global.interceptor;

import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.CookieUtils;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * CookieInterceptor
 * <pre>
 *  controller -> view로 가는 중간에 작용하는 인터셉터
 *  브라우저 쿠키 및 로컬 스토리지 관련 로직 처리
 * </pre>
 *
 * @author nichefish
 */
@Component
@Log4j2
public class CookieInterceptor
        implements HandlerInterceptor {

    /**
     * postHandle : controller 요청 처리 후 view를 렌더링하기 전에 동작한다.
     * true = 통과, false = 미통과
     */
    @Override
    public void postHandle(
            final @NotNull HttpServletRequest request,
            final @NotNull HttpServletResponse response,
            final @NotNull Object handler,
            final ModelAndView mav
    ) {

        final String requestURI = request.getRequestURI();
        if (Url.AUTH_LGN_FORM.equals(requestURI) || Url.AUTH_LGN_PROC.equals(requestURI)) return;

        /* 사이드바 접기 쿠키 설정 */
        if (mav != null) {
            final String sidebarMinimizeState = CookieUtils.getCookie(Constant.KT_SIDEBAR_MINIMIZE_STATE);
            final boolean isSidebarMinimized = "on".equals(sidebarMinimizeState);
            mav.addObject("isSidebarMinimized", isSidebarMinimized);
        }

        /* 응답 내려갈 때마다 항상 blockUI 클리어 쿠키를 내려준다. */
        CookieUtils.setResponseSuccessCookie();
    }
}
