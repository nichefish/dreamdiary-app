package io.nicheblog.dreamdiary.infrastructure.web.interceptor;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Vue 프로덕션 빌드 해시 자산의 브라우저 캐시 헤더를 설정한다.
 *
 * <p>{@code /vue-app/assets/**} 파일명은 콘텐츠 해시를 포함하므로 같은 URL의 내용이 불변이다.
 * SPA 진입점과 fallback 응답은 이 interceptor의 등록 경로에 포함되지 않는다.</p>
 */
@Component
public class VueAssetCacheControlInterceptor implements HandlerInterceptor {

    static final String CACHE_CONTROL_VALUE = "public, max-age=31536000, immutable";

    /**
     * 해시 자산 응답에 장기 브라우저 캐시 계약을 기록한다.
     *
     * @param request 현재 HTTP 요청
     * @param response 현재 HTTP 응답
     * @param handler 선택된 MVC handler
     * @return 요청 처리를 계속하므로 항상 {@code true}
     */
    @Override
    public boolean preHandle(
            final @NotNull HttpServletRequest request,
            final @NotNull HttpServletResponse response,
            final @NotNull Object handler
    ) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_VALUE);
        return true;
    }
}
