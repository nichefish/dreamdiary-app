package io.nicheblog.dreamdiary.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HttpUtils
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class HttpUtils {

    /**
     * 브라우저의 캐시를 초기화합니다.
     *
     * @param response 응답 객체
     */
    public static void setInvalidateBrowserCacheHeader(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
    }

    /**
     * AJAX 요청 여부 판단
     * (헤더의 X-Requested-With 값으로 판단)
     *
     * @param request {@link HttpServletRequest} 클라이언트의 요청 객체
     * @return AJAX 요청 여부
     */
    public static boolean isAjaxRequest(final HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    /**
     * AJAX 요청 여부 판단
     * (헤더의 X-Requested-With 값으로 판단)
     *
     * @param request {@link WebRequest} 스프링의 웹 요청 객체
     * @return AJAX 요청 여부
     */
    public static boolean isAjaxRequest(final WebRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }
}
