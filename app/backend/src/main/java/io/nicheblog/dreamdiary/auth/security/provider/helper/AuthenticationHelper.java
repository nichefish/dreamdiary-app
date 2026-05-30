package io.nicheblog.dreamdiary.auth.security.provider.helper;

import io.nicheblog.dreamdiary.auth.policy.entity.AuthPolicyEntity;
import io.nicheblog.dreamdiary.auth.policy.service.AuthPolicyQueryService;
import io.nicheblog.dreamdiary.auth.security.exception.AccountDormantException;
import io.nicheblog.dreamdiary.auth.security.exception.AccountNeedsPwResetException;
import io.nicheblog.dreamdiary.auth.security.exception.DupIdLoginException;
import io.nicheblog.dreamdiary.auth.security.exception.IpNotAllowedException;
import io.nicheblog.dreamdiary.auth.security.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.security.service.manager.DupIdLoginManager;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.user.account.service.UserService;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

/**
 * AuthenticationHelper
 * <pre>
 *  Spring Security :: 사용자 인증 로직 분리
 *  비밀번호 체크 + 접속IP + 비밀번호 변경기간 체크기능 추가하여 구현
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class AuthenticationHelper {

    private final UserService userService;
    private final AuthPolicyQueryService authPolicyQueryService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 주어진 인증 정보를 기반으로 사용자를 인증합니다.
     * 계정 기본 정보 유효성 검사
     *
     * @param authentication 인증 정보를 담고 있는 {@link Authentication} 객체
     * @param authInfo 인증된 사용자 정보
     * @return {@link Boolean} -- 인증 체크 성공 여부
     */
    public Boolean validateAuth(final Authentication authentication, final AuthInfo authInfo) throws Exception {

        // 계정 존재여부 체크
        if (authentication == null || authInfo == null) throw new InternalAuthenticationServiceException("exception.Exception");

        // 중복 로그인 '확인'(기존 아이디 끊기) 후 들어왔을 시 바로 패스 :: 메소드 분리
        final String username = authentication.getName();
        if (this.isDupLoginConfirmed(username)) return true;

        // password 일치여부 체크
        final String password = (String) authentication.getCredentials();
        if (!passwordEncoder.matches(password, authInfo.getPassword())) throw new BadCredentialsException("exception.BadCredentialsException");

        authInfo.nullifyPasswordInfo();
        return this.validateAuth(authInfo);
    }

    /**
     * 주어진 인증 정보를 기반으로 사용자를 인증합니다. (중복 로그인, 패스워드 비교 제외)
     * 계정 상세 정보 유효성 검사
     *
     * @param authInfo 인증된 사용자 정보
     * @return {@link Boolean} -- 인증 체크 성공 여부
     */
    public Boolean validateAuth(final AuthInfo authInfo) throws Exception {
        if (authInfo == null) throw new UsernameNotFoundException("exception.UsernameNotFoundException");

        final String username = authInfo.getUsername();

        // 장기간 미로그인여부 체크 :: 시스템계정"system"은 제외
        if (userService.isDormant(username)) throw new AccountDormantException("AbstractUserDetailsAuthenticationProvider.AccountDormantException");

        // 잠금여부 체크
        if ("Y".equals(authInfo.getLockedYn())) {
            final Date lockExpiresAt = authInfo.getLockExpiresAt();
            final boolean isStillLocked = (lockExpiresAt == null) || lockExpiresAt.after(DateUtils.getCurrDate());
            if (isStillLocked) throw new LockedException("AbstractUserDetailsAuthenticationProvider.LockedException");
        }

        // 접속IP 체크 :: 메소드 분리
        if (!this.isAllowedIpValid(authInfo)) throw new IpNotAllowedException("AbstractUserDetailsAuthenticationProvider.IpNotAllowedException");

        // 비밀번호 만료 여부 체크
        if (!this.isPwExpryValid(authInfo)) throw new CredentialsExpiredException("AbstractUserDetailsAuthenticationProvider.CredentialsExpiredException");

        // 비밀번호 변경 필요 여부 체크
        final boolean needsPasswordReset = "Y".equals(authInfo.getNeedsPasswordReset());
        if (needsPasswordReset) {
            if (!this.isPwResetTokenValid(authInfo)) {
                throw new CredentialsExpiredException("AbstractUserDetailsAuthenticationProvider.CredentialsExpiredException");
            }
            final String passwordToken = userService.issuePasswordResetToken(username);
            throw new AccountNeedsPwResetException("AbstractUserDetailsAuthenticationProvider.AccountNeedsPwResetException", passwordToken);
        }

        // 중복 로그인 체크 :: 세션 attribute 훑어서 "loginId" 비교
        final boolean isDupLogin = DupIdLoginManager.isDupIdLogin(username);
        if (isDupLogin) throw new DupIdLoginException("AbstractUserDetailsAuthenticationProvider.DupIdLoginException");

        return true;
    }

    /**
     * 중복 로그인 후 confirm 눌러 재접속 여부 :: 메소드 분리
     *
     * @param username 확인할 사용자 이름 (String)
     * @return {@link Boolean} -- 중복 로그인 후 재접근이 확인된 경우 true, 그렇지 않으면 false
     */
    public Boolean isDupLoginConfirmed(final String username) {
        if (StringUtils.isEmpty(username)) return false;

        final ServletRequestAttributes servletRequestAttribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        final HttpSession session = servletRequestAttribute.getRequest().getSession(false);
        if (session == null) return false;

        final Object isDupIdLogin = session.getAttribute("isDupIdLogin");
        session.removeAttribute("isDupIdLogin");
        return isDupIdLogin instanceof String && username.equals(isDupIdLogin);
    }

    /**
     * 접속 IP 체크 :: 메소드 분리
     *
     * @param authInfo 사용자 인증 정보 (AuthInfo)
     * @return {@link Boolean} -- 접속 IP가 유효한 경우 true
     */
    public Boolean isAllowedIpValid(final AuthInfo authInfo) {
        if (!"Y".equals(authInfo.getUseAllowedIpYn())) return true;

        final List<String> allowedIpStrList = authInfo.getAllowedIpStrList();
        if (CollectionUtils.isEmpty(allowedIpStrList)) return true;

        final String remoteAddr = AuthUtils.getRemoteIpAddr();
        log.info("logged in remoteAddr: {}", remoteAddr);

        // 순회하며 IP 체크
        for (final String allowedIp : allowedIpStrList) {
            log.info("comparing remoteIP {} to access-allowed-IP {}...", remoteAddr, allowedIp);
            final boolean isCidr = allowedIp.contains("/");
            if (!isCidr) {
                // 단순 IP일 경우: 정확히 일치여부 확인
                if (allowedIp.equals(remoteAddr)) return true;
            } else {
                // CIDR일 경우: 범위 체크
                final SubnetUtils subnetUtils = new SubnetUtils(allowedIp);
                final boolean isIpAddrWithinValid = subnetUtils.getInfo().isInRange(remoteAddr);
                if (isIpAddrWithinValid) return true;
            }
        }
        return false;
    }

    /**
     * 비밀번호 만료 여부 체크 :: 메소드 분리
     *
     * @param authInfo 사용자 인증 정보 (AuthInfo)
     * @return {@link Boolean} -- 비밀번호가 만료되지 않은 경우 true
     */
    public Boolean isPwExpryValid(final AuthInfo authInfo) throws Exception {
        final AuthPolicyEntity authPolicy = authPolicyQueryService.getDtlEntity();
        final Integer passwordChangeCycleDays = authPolicy.getPasswordChangeCycleDays();
        final Date pwExprDt = DateUtils.getDateAddDay(authInfo.getPasswordChangedAt(), passwordChangeCycleDays);
        final boolean isPwExprd = (pwExprDt == null || pwExprDt.compareTo(DateUtils.getCurrDate()) < 0);
        return !isPwExprd;
    }

    /**
     * 패스워드 리셋 토큰 만료 여부 체크.
     */
    public Boolean isPwResetTokenValid(final AuthInfo authInfo) throws Exception {
        final AuthPolicyEntity authPolicy = authPolicyQueryService.getDtlEntity();
        final Integer expiryMinutes = (authPolicy == null || authPolicy.getPasswordResetTokenExpiryMinutes() == null)
                ? 30
                : authPolicy.getPasswordResetTokenExpiryMinutes();
        final Date issuedAt = authInfo.getPasswordResetTokenIssuedAt();
        if (issuedAt == null) return false;

        final Date expiresAt = new Date(issuedAt.getTime() + (expiryMinutes.longValue() * 60L * 1000L));
        return expiresAt.after(DateUtils.getCurrDate());
    }
}
