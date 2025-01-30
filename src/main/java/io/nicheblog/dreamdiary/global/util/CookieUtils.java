package io.nicheblog.dreamdiary.global.util;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * CookieUtils
 * <pre>
 *  브라우저 쿠키 처리 서비스 모듈
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

    /** static 맥락에서 사용할 수 있도록 bean 주입 */
    @PostConstruct
    private void init() {
        request = autowiredRequest;
        response = autowiredResponse;
    }

    private static final Integer A_DAY = 60 * 60 * 24;

    /**
     * 공통 > 쿠키 생성
     */
    public static void setCookie(final String name, final String value) {
        // response 맥락 하에서만 실행
        if (response == null) return;

        setCookie(name, value, 60 * 60 * 24);
        final Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(A_DAY);     //쿠키 유효 기간: 하루로 설정(60초 * 60분 * 24시간)
        cookie.setPath("/");            //모든 경로에서 접근 가능하도록 설정
        response.addCookie(cookie);
    }

    /**
     * 공통 > 쿠키 생성
     */
    public static void setCookie(final String name, final String value, Integer age) {
        // response 맥락 하에서만 실행
        if (response == null) return;

        final Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(age);     //쿠키 유효 기간: 하루로 설정(60초 * 60분 * 24시간)
        cookie.setPath("/");            //모든 경로에서 접근 가능하도록 설정
        response.addCookie(cookie);
    }

    /**
     * JWT 인증 쿠키 생성
     * "쿠키의 Expires 또는 Max-Age 속성을 설정하지 않으면, 브라우저는 해당 쿠키를 세션 쿠키로 처리합니다. 즉, 브라우저를 닫으면 쿠키가 삭제됩니다."
     *
     * @param jwt 설정할 JWT 토큰 값
     */
    public static void setJwtCookie(final String jwt) {
        // response 맥락 하에서만 실행
        if (response == null) return;

        final Cookie cookie = new Cookie("jwt", jwt);
        cookie.setHttpOnly(true);   // JavaScript에서 접근 불가
        // cookie.setSecure(true);     // HTTPS 환경에서만 작동
        cookie.setPath("/");            //모든 경로에서 접근 가능하도록 설정
        response.addCookie(cookie);
    }

    /**
     * 공통 > 파일 생성 성공 쿠키 생성
     */
    public static void setFileDownloadSuccessCookie() {
        // response 맥락 하에서만 실행
        if (response == null) return;

        final Cookie cookie = new Cookie("FILE_CREATE_SUCCESS", "TRUE");
        cookie.setMaxAge(3);            //쿠키 유효 기간: 3초
        cookie.setPath("/");            //모든 경로에서 접근 가능하도록 설정
        response.addCookie(cookie);
    }

    /**
     * 공통 > 응답 처리 완료 쿠키 생성
     */
    public static void setResponseSuccessCookie() {
        // response 맥락 하에서만 실행
        if (response == null) return;

        final Cookie cookie = new Cookie("RESPONSE_SUCCESS", "TRUE");
        cookie.setMaxAge(3);            //쿠키 유효 기간: 3초
        cookie.setPath("/");            //모든 경로에서 접근 가능하도록 설정
        response.addCookie(cookie);
    }

    /**
     * 공통 > 특정 쿠키 조회
     *
     * @param name 조회할 쿠키의 이름
     */
    public static String getCookie(final String name) {
        // request 맥락 하에서만 실행
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
     * 공통 > 특정 쿠키 삭제
     *
     * @param name 삭제할 쿠키의 이름
     */
    public static void deleteCookie(final String name) {
        // response 맥락 하에서만 실행
        if (response == null) return;

        final Cookie cookie = new Cookie(name, null); // 삭제할 쿠키에 대한 값을 null로 지정
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /**
     * 공통 > 모든 쿠키 삭제
     */
    public static void deleteAllCookies() {
        // response 맥락 하에서만 실행
        if (request == null || response == null) return;

        final Cookie[] cookies = request.getCookies(); // 모든 쿠키의 정보를 cookies에 저장
        if (cookies == null || cookies.length == 0) return;

        Arrays.stream(cookies)
              .forEach(cookie -> {
                  cookie.setMaxAge(0);            // 유효시간을 0으로 설정
                  response.addCookie(cookie);     // 응답에 추가하여 만료시키기.
              });
    }
}
