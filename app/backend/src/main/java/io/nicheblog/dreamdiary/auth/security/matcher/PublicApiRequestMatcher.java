package io.nicheblog.dreamdiary.auth.security.matcher;

import io.nicheblog.dreamdiary.global.Url;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/** 비로그인 상태에서 접근 가능한 API 경로와 HTTP 메서드를 단일 목록으로 관리한다. */
@Component
public class PublicApiRequestMatcher implements RequestMatcher {

    private final RequestMatcher delegate = new OrRequestMatcher(List.of(
            new AntPathRequestMatcher(Url.API_AUTH_LOGIN, "POST"),
            new AntPathRequestMatcher(Url.API_AUTH_LGN_PROC, "POST"),
            new AntPathRequestMatcher(Url.API_AUTH_REFRESH, "POST"),
            new AntPathRequestMatcher(Url.API_AUTH_LGOUT_JSON, "POST"),
            new AntPathRequestMatcher(Url.API_AUTH_EXPIRE_SESSION, "POST"),
            new AntPathRequestMatcher(Url.API_AUTH_LGN_PW_CHG, "POST"),
            new AntPathRequestMatcher(Url.API_AUTH_VERIFY.replace("{token}", "**"), "GET"),
            new AntPathRequestMatcher(Url.USERS_DUPLICATE_USERNAME_CHECK, "GET"),
            new AntPathRequestMatcher(Url.USERS_DUPLICATE_EMAIL_CHECK, "GET"),
            new AntPathRequestMatcher(Url.USER_SIGNUP_REQUESTS, "POST")
    ));

    /**
     * 요청 경로와 HTTP 메서드가 공개 API 계약에 포함되는지 확인한다.
     *
     * @param request HTTP 요청
     * @return 공개 API이면 true
     */
    @Override
    public boolean matches(final HttpServletRequest request) {
        return delegate.matches(request);
    }
}
