package io.nicheblog.dreamdiary.global._common.auth.util;

import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global._common.auth.model.AuthInfo;
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
        final boolean isAuthenticated = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
        if (!isAuthenticated) log.info("isAuthenticated: {}", isAuthenticated);
        return isAuthenticated;
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
    public static Integer getAuthenticatedUserProflNo() {
        if (RequestContextHolder.getRequestAttributes() == null) return null;
        final AuthInfo authInfo = getAuthenticatedUser();
        assert authInfo != null;
        return (authInfo.getProfl() == null) ? null : authInfo.getProfl().getUserProflNo();
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
    public static String getLgnUserId() {
        final AuthInfo authInfo = getAuthenticatedUser();
        assert authInfo != null;
        return authInfo.getUserId();
    }

    /**
     * 특정 객체에 대해 내 정보 여부를 체크해서 반환한다.
     *
     * @return {@link Boolean} -- 내가 작성한 정보일 경우 true.
     */
    public static Boolean isMyInfo(final String paramUserId) {
        if (paramUserId == null) return false;
        final AuthInfo authInfo = getAuthenticatedUser();
        assert authInfo != null;
        final String myUserId = authInfo.getUserId();
        return paramUserId.equals(myUserId);
    }

    /**
     * 특정 객체에 대해 특정 ID의 등록자 여부를 체크해서 반환한다.
     *
     * @return {@link Boolean} -- 해당 ID가 등록한 정보일 경우 true.
     */
    public static Boolean isRegstr(final String regstrId) {
        if (StringUtils.isEmpty(regstrId)) return false;
        final AuthInfo authInfo = getAuthenticatedUser();
        if (authInfo == null) return false;

        final String myUserId = authInfo.getUserId();
        return regstrId.equals(myUserId);
    }

    /**
     * 특정 객체에 대해 특정 ID의 수정자 여부를 체크해서 반환한다.
     *
     * @return {@link Boolean} -- 해당 ID가 수정한 정보일 경우 true.
     */
    public static Boolean isMdfusr(final String mdfusrId) {
        return isRegstr(mdfusrId);
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
        log.info("ipAddr > {}: {}", ipType, ipAddr);
        return ipAddr;
    }
}