package io.nicheblog.dreamdiary.infrastructure.web.util;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * CookieUtils
 * <pre>
 *  브라우저 쿠키 처리 유틸
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class CookieUtils {

    private final HttpServletRequest autowiredRequest;
    private final HttpServletResponse autowiredResponse;

    private static HttpServletRequest request;
    private static HttpServletResponse response;

    /** static 메소드에서 사용할 수 있도록 요청/응답 객체를 주입한다. */
    @PostConstruct
    private void init() {
        request = autowiredRequest;
        response = autowiredResponse;
    }

    private static final Integer A_DAY = 60 * 60 * 24;
    public static final String JWT_COOKIE_NAME = "jwt";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    /**
     * 공통 쿠키를 생성한다.
     */
    public static void setCookie(final String name, final String value) {
        // 응답 객체가 있을 때만 실행한다.
        if (response == null) return;

        setCookie(name, value, 60 * 60 * 24);
        final Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(A_DAY);     // 쿠키 유효 기간: 하루
        cookie.setPath("/");        // 모든 경로에서 접근 가능하도록 설정
        response.addCookie(cookie);
    }

    /**
     * JWT 쿠키를 설정한다.
     *
     * @param jwt 설정할 JWT 토큰 값
     * @param maxAgeSec 유효 기간(초). -1이면 세션 쿠키
     */
    public static void setJwtCookie(final String jwt, final int maxAgeSec) {
        addHttpOnlyCookie(JWT_COOKIE_NAME, jwt, maxAgeSec);
    }

    /**
     * Refresh Token 쿠키를 설정한다.
     *
     * @param refreshToken 리프레시 토큰 문자열
     * @param maxAgeSec 유효 기간(초)
     */
    public static void setRefreshTokenCookie(final String refreshToken, final int maxAgeSec) {
        addHttpOnlyCookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken, maxAgeSec);
    }

    /**
     * JWT 쿠키를 삭제한다.
     */
    public static void deleteJwtCookie() {
        addHttpOnlyCookie(JWT_COOKIE_NAME, "", 0);
    }

    /**
     * Refresh Token 쿠키를 삭제한다.
     */
    public static void deleteRefreshTokenCookie() {
        addHttpOnlyCookie(REFRESH_TOKEN_COOKIE_NAME, "", 0);
    }

    /**
     * 공통 쿠키를 생성한다.
     */
    public static void setCookie(final String name, final String value, Integer age) {
        // 응답 객체가 있을 때만 실행한다.
        if (response == null) return;

        final Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(age);      // 쿠키 유효 기간
        cookie.setPath("/");       // 모든 경로에서 접근 가능하도록 설정
        response.addCookie(cookie);
    }

    /**
     * JWT 인증 쿠키를 생성한다.
     * 만료 속성을 지정하지 않으면 브라우저는 세션 쿠키로 처리한다.
     *
     * @param jwt 설정할 JWT 토큰 값
     */
    public static void setJwtCookie(final String jwt) {
        // 응답 객체가 있을 때만 실행한다.
        if (response == null) return;

        final Cookie cookie = new Cookie("jwt", jwt);
        cookie.setHttpOnly(true);   // JavaScript에서 접근 불가
        // cookie.setSecure(true);  // HTTPS 환경에서만 동작
        cookie.setPath("/");       // 모든 경로에서 접근 가능하도록 설정
        response.addCookie(cookie);
    }

    /**
     * 파일 생성 성공 쿠키를 생성한다.
     */
    public static void setFileDownloadSuccessCookie() {
        // 응답 객체가 있을 때만 실행한다.
        if (response == null) return;

        final Cookie cookie = new Cookie("FILE_CREATE_SUCCESS", "TRUE");
        cookie.setMaxAge(3);       // 쿠키 유효 기간: 3초
        cookie.setPath("/");       // 모든 경로에서 접근 가능하도록 설정
        response.addCookie(cookie);
    }

    /**
     * 응답 처리 완료 쿠키를 생성한다.
     */
    public static void setResponseSuccessCookie() {
        // 응답 객체가 있을 때만 실행한다.
        if (response == null) return;

        final Cookie cookie = new Cookie("RESPONSE_SUCCESS", "TRUE");
        cookie.setMaxAge(3);       // 쿠키 유효 기간: 3초
        cookie.setPath("/");       // 모든 경로에서 접근 가능하도록 설정
        response.addCookie(cookie);
    }

    /**
     * 특정 쿠키를 조회한다.
     *
     * @param name 조회할 쿠키 이름
     */
    public static String getCookie(final String name) {
        // 요청 객체가 있을 때만 실행한다.
        if (request == null) return null;
        if (StringUtils.isEmpty(name)) return null;

        final Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (final Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    /**
     * 특정 쿠키를 삭제한다.
     *
     * @param name 삭제할 쿠키 이름
     */
    public static void deleteCookie(final String name) {
        // 응답 객체가 있을 때만 실행한다.
        if (response == null) return;

        final Cookie cookie = new Cookie(name, null); // 삭제할 쿠키 값을 비운다.
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /**
     * 모든 쿠키를 삭제한다.
     */
    public static void deleteAllCookies() {
        // 요청/응답 객체가 있을 때만 실행한다.
        if (request == null || response == null) return;

        final Cookie[] cookies = request.getCookies(); // 모든 쿠키 정보를 가져온다.
        if (cookies == null || cookies.length == 0) return;

        Arrays.stream(cookies)
              .forEach(cookie -> {
                  cookie.setMaxAge(0);        // 유효 시간을 0으로 설정한다.
                  response.addCookie(cookie); // 응답에 추가하여 만료시킨다.
              });
    }

    /**
     * HTTP 전용 쿠키를 설정한다.
     *
     * @param name 쿠키 이름
     * @param value 쿠키 값
     * @param maxAgeSec 유효 기간(초)
     */
    private static void addHttpOnlyCookie(final String name, final String value, final int maxAgeSec) {
        if (response == null) return;

        final boolean secure = request != null && request.isSecure();
        final String safeValue = value == null ? "" : value;
        final ResponseCookie cookie = ResponseCookie.from(name, safeValue)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite("Lax")
                .maxAge(maxAgeSec)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
