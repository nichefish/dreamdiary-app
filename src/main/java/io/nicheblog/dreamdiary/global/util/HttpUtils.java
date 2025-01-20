package io.nicheblog.dreamdiary.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class HttpUtils {

    private final HttpServletResponse autowiredResponse;

    private static HttpServletResponse response;

    /** static 맥락에서 사용할 수 있도록 bean 주입 */
    @PostConstruct
    private void init() {
        response = autowiredResponse;
    }

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
     */
    public static boolean isAjaxRequest(final HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    /**
     * AJAX 요청 여부 판단
     */
    public static boolean isAjaxRequest(final WebRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }
}
