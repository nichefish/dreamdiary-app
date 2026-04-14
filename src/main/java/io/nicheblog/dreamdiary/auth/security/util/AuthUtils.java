package io.nicheblog.dreamdiary.auth.security.util;

import io.nicheblog.dreamdiary.auth.security.model.AuthInfo;
import io.nicheblog.dreamdiary.global.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

/**
 * AuthUtils
 * <pre>
 *  Spring Security:: 인증 및 권한 처리 관련 유틸리티 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class AuthUtils {

    private final HttpServletRequest autowiredRequest;

    private static HttpServletRequest request;

    /** static 맥락에서 사용할 수 있도록 bean 주입 */
    @PostConstruct
    private void init() {
        request = autowiredRequest;
    }

    /**
     * 현재 사용자의 인증 여부를 조회해서 반환한다.
     *
     * @return {@link Boolean} -- 인증 상태일 경우 true. 익명 사용자(anynymousUser)의 경우 false.
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 현재 사용자의 인증 여부를 조회해서 반환한다.
     *
     * @return {@link Boolean} -- 인증 상태일 경우 true. 익명 사용자(anynymousUser)의 경우 false.
     */
    public static Boolean isAuthenticated() {
        final Authentication auth = getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    /**
     * 현재 로그인 중인 사용자 정보를 세션에서 조회해서 반환한다.
     *
     * @return {@link AuthInfo} -- 현재 로그인 중인 사용자 인증정보 객체
     */
    public static AuthInfo getAuthenticatedUser() {
        if (!isAuthenticated()) return null;
        return (AuthInfo) getAuthentication().getPrincipal();
    }

    /**
     * 현재 로그인 중인 사용자 프로필 정보 번호를 조회해서 반환한다.
     *
     * @return {@link Integer} -- 현재 로그인 중인 사용자 프로필 정보 번호
     */
    public static Integer getAuthenticatedUserProfileId() {
        if (RequestContextHolder.getRequestAttributes() == null) return null;
        final AuthInfo authInfo = getAuthenticatedUser();
        assert authInfo != null;
        return (authInfo.getProfile() == null) ? null : authInfo.getProfile().getUserProfileId();
    }
    
    /**
     * 현재 로그인 중인 사용자 이름을 반환한다.
     * 
     * @return {@link String} -- 현재 로그인 중인 사용자 이름
     */
    public static String getLgnUserNm() {
        final AuthInfo AuthInfo = getAuthenticatedUser();
        assert AuthInfo != null;
        return AuthInfo.getNickNm();
    }

    /**
     * 현재 로그인 중인 사용자 어이디를 반환한다.
     *
     * @return {@link String} -- 현재 로그인 중인 사용자 아이디
     */
    public static String getLgnUsername() {
        final AuthInfo authInfo = getAuthenticatedUser();
        if (authInfo == null) return null;
        return authInfo.getUsername();
    }

    /**
     * username 유효성 확인 (blank 불가)
     *
     * @param username 사용자 계정명
     * @return {@link String} -- 검증된 사용자 계정명
     */
    public static String requireUsername(final String username) {
        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("username is required.");
        }
        return username;
    }

    /**
     * 현재 로그인 중인 사용자 아이디를 검증하여 반환한다.
     *
     * @return {@link String} -- 검증된 현재 로그인 사용자 아이디
     */
    public static String requireLgnUsername() {
        return requireUsername(getLgnUsername());
    }

    /**
     * 현재 로그인 중인 사용자 어이디를 반환한다.
     *
     * @return {@link String} -- 현재 로그인 중인 사용자 아이디
     */
    public static String getLgnUsernameOrDefault() {
        if (!isAuthenticated()) return Constant.SYSTEM_ACNT;
        return getLgnUsername();
    }

    /**
     * 특정 객체에 대해 내 정보 여부를 체크해서 반환한다.
     *
     * @return {@link Boolean} -- 내가 작성한 정보일 경우 true.
     */
    public static Boolean isMyInfo(final String paramUsername) {
        if (paramUsername == null) return false;
        final AuthInfo authInfo = getAuthenticatedUser();
        assert authInfo != null;
        final String myUsername = authInfo.getUsername();
        return paramUsername.equals(myUsername);
    }

    /**
     * 특정 객체에 대해 특정 ID의 등록자 여부를 체크해서 반환한다.
     *
     * @return {@link Boolean} -- 해당 ID가 등록한 정보일 경우 true.
     */
    public static Boolean isCreatedBy(final String createdBy) {
        if (StringUtils.isEmpty(createdBy)) return false;
        final AuthInfo authInfo = getAuthenticatedUser();
        if (authInfo == null) return false;

        final String myUsername = authInfo.getUsername();
        return createdBy.equals(myUsername);
    }

    /**
     * 특정 객체에 대해 특정 ID의 수정자 여부를 체크해서 반환한다.
     *
     * @return {@link Boolean} -- 해당 ID가 수정한 정보일 경우 true.
     */
    public static Boolean isUpdatedBy(final String updatedBy) {
        return isCreatedBy(updatedBy);
    }

    /**
     * 인증 처리
     */
    public static void setAuthentication(final Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 공통 > 특정 권한 보유 여부 체크
     *
     * @return {@link Boolean} -- 해당 권한 보유시 true.
     */
    public static Boolean hasAuthority(final String roleStr) {
        final AuthInfo authInfo = getAuthenticatedUser();
        assert authInfo != null;
        for (final GrantedAuthority grantedAuthority : authInfo.getAuthorities()) {
            if (roleStr.equals(grantedAuthority.getAuthority())) return true;
        }
        return false;
    }

    /**
     * 사용자 IP 주소 조회 (헤더 조회)
     *
     * @return {@link String} -- 현재 로그인 중인 사용자가 접속 중인 IP 주소.
     */
    public static String getAcsIpAddr() {
        // request 맥락 하에서만 실행
        if (request == null) return null;
        String ipType = "";
        String ipAddr = "";
        for (final String s : Constant.IP_HEADERS) {
            ipType = s;
            ipAddr = request.getHeader(ipType);
            if (ipAddr != null) break;
        }
        if (ipAddr == null) {
            ipType = Constant.REMOTE_ADDR;
            ipAddr = request.getRemoteAddr();
        }
        return ipAddr;
    }

}
