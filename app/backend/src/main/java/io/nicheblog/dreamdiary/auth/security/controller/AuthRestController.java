package io.nicheblog.dreamdiary.auth.security.controller;

import io.jsonwebtoken.JwtException;
import io.nicheblog.dreamdiary.auth.jwt.provider.JwtTokenProvider;
import io.nicheblog.dreamdiary.auth.jwt.service.RefreshTokenService;
import io.nicheblog.dreamdiary.auth.policy.entity.AuthPolicyEntity;
import io.nicheblog.dreamdiary.auth.policy.service.AuthPolicyQueryService;
import io.nicheblog.dreamdiary.auth.security.exception.AccountDormantException;
import io.nicheblog.dreamdiary.auth.security.exception.AccountNeedsPwResetException;
import io.nicheblog.dreamdiary.auth.security.exception.DupIdLoginException;
import io.nicheblog.dreamdiary.auth.security.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.security.model.AuthUserDto;
import io.nicheblog.dreamdiary.auth.security.service.AuthSessionPolicyService;
import io.nicheblog.dreamdiary.auth.security.service.manager.DupIdLoginManager;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.auth.security.service.AuthService;
import io.nicheblog.dreamdiary.feature.user.account.model.UserPwChgParam;
import io.nicheblog.dreamdiary.feature.user.my.service.UserMyService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.event.LoginSuccessCacheWarmupEvent;
import io.nicheblog.dreamdiary.infrastructure.log.event.LogAnonymousEvent;
import io.nicheblog.dreamdiary.infrastructure.log.event.LogEvent;
import io.nicheblog.dreamdiary.infrastructure.log.model.LogParam;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import io.nicheblog.dreamdiary.infrastructure.web.util.CookieUtils;
import io.nicheblog.dreamdiary.infrastructure.web.util.HttpUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.nicheblog.dreamdiary.auth.security.provider.DreamdiaryAuthenticationProvider;
import javax.annotation.security.PermitAll;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AuthRestController
 * <pre>
 *  인증 관련 REST 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class AuthRestController {

    @Getter
    private final String baseUrl = Url.APP_AUTH_LGN_FORM;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.LGN;      // 작업 카테고리

    private final UserMyService userMyService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final DreamdiaryAuthenticationProvider authenticationProvider;
    private final AuthPolicyQueryService authPolicyQueryService;
    private final AuthSessionPolicyService authSessionPolicyService;
    private final ApplicationEventPublisherWrapper publisher;

    /**
     * 인증 정보를 조회한다.
     * JWT 토큰을 검증하고 사용자 정보를 추출한다.
     *
     * @param request HTTP 요청 객체
     * @return {@link ResponseEntity} 처리 결과와 메시지
     */
    @GetMapping(Url.API_AUTH_INFO)
    public ResponseEntity<AjaxResponse> getAuthInfo(
            final HttpServletRequest request
    ) {

        try {
            // JWT 검증 및 사용자 정보 추출
            final String jwtToken = jwtTokenProvider.resolveToken(request);
            if (StringUtils.isBlank(jwtToken)) return unauthorizedAndInvalidate(request);

            final Authentication authentication = jwtTokenProvider.getDirectAuthentication(jwtToken);
            final AuthInfo authInfo = (AuthInfo) authentication.getPrincipal();

            return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withObj(AuthUserDto.from(authInfo)));
        } catch (final JwtException | AuthenticationException e) {
            return unauthorizedAndInvalidate(request);
        } catch (final Exception e) {
            // 그 외 일반적인 예외 처리
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AjaxResponse.withAjaxResult(false, MessageUtils.RSLT_FAILURE));
        }
    }

    /**
     * Vue SPA modal/action preflight.
     * Checks the current authenticated username without returning user details.
     *
     * @param request HTTP request
     * @return {@link ResponseEntity} processing result
     */
    @GetMapping(Url.API_SESSION_PING)
    @ResponseBody
    public ResponseEntity<AjaxResponse> sessionPingAjax(
            final HttpServletRequest request
    ) {

        try {
            AuthUtils.requireLoginUsername();
            return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS));
        } catch (final Exception e) {
            log.info("Session ping rejected. reason={}", e.getClass().getSimpleName());
            return unauthorizedAndInvalidate(request);
        }
    }

    /**
     * Refresh Token을 재발급한다.
     *
     * @param request HTTP 요청 객체
     * @return {@link ResponseEntity} 처리 결과와 메시지
     */
    @PostMapping(Url.API_AUTH_REFRESH)
    @PermitAll
    @ResponseBody
    public ResponseEntity<AjaxResponse> refreshToken(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {

        try {
            final String refreshToken = CookieUtils.getCookie(CookieUtils.REFRESH_TOKEN_COOKIE_NAME);
            if (StringUtils.isBlank(refreshToken)) {
                return unauthorizedAndInvalidate(request);
            }

            final RefreshTokenService.RefreshResult refreshResult = refreshTokenService.rotate(refreshToken);
            final AuthInfo authInfo = authService.loadUserByUsername(refreshResult.getUsername());
            final List<String> roles = authInfo.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            final String accessToken = jwtTokenProvider.createAccessToken(authInfo.getUsername(), roles);

            CookieUtils.setJwtCookie(accessToken, authSessionPolicyService.getSessionTimeoutSecondsAsInt());
            CookieUtils.setRefreshTokenCookie(refreshResult.getRefreshToken(), (int) refreshTokenService.getRefreshTokenTtlSeconds());
            if (response != null) response.setHeader("Authorization", "Bearer " + accessToken);

            final HttpSession session = request.getSession(false);
            if (session != null) {
                authSessionPolicyService.applyToSession(session);
                session.setAttribute("jwt", accessToken);
            }

            return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS));
        } catch (final JwtException | AuthenticationException e) {
            return unauthorizedAndInvalidate(request);
        } catch (final Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AjaxResponse.withAjaxResult(false, MessageUtils.RSLT_FAILURE));
        }
    }


    /**
     * 비밀번호 강제 변경을 처리한다.
     * 만료 비밀번호 또는 비밀번호 리셋 상황에서 비로그인 사용자도 접근할 수 있다.
     *
     * @param userPwChgParam 비밀번호 변경 파라미터
     * @return {@link ResponseEntity} 처리 결과와 메시지
     */
    @PostMapping(Url.API_AUTH_LGN_PW_CHG)
    @PermitAll
    @ResponseBody
    public ResponseEntity<AjaxResponse> loginPwChgAjax(
            final @Valid UserPwChgParam userPwChgParam
    ) throws Exception {

        final boolean isSuccess = userMyService.loginPwChg(userPwChgParam);
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }

    /**
     * 세션 강제 만료를 처리한다.
     * 중복 로그인에서 기존 아이디 끊기를 취소한 경우 사용한다.
     *
     * @param request HTTP 요청 객체
     * @return {@link ResponseEntity} 처리 결과와 메시지
     */
    @PostMapping(Url.API_AUTH_EXPIRE_SESSION)
    @PermitAll
    @ResponseBody
    public ResponseEntity<AjaxResponse> expireSessionAjax(
            final HttpServletRequest request
    ) {

        // 세션 만료 처리
        final HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS));
    }

    /**
     * 인증 실패 응답을 반환하고 인증 상태를 무효화한다.
     *
     * @param request HTTP 요청 객체
     * @return {@link ResponseEntity} 처리 결과와 메시지
     */
    private ResponseEntity<AjaxResponse> unauthorizedAndInvalidate(final HttpServletRequest request) {
        invalidateAuthentication(request);
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(AjaxResponse.withAjaxResult(false, MessageUtils.getExceptionMsg("AuthenticationFailureException")));
    }

    /**
     * 인증 상태를 무효화한다.
     *
     * @param request HTTP 요청 객체
     */
    private void invalidateAuthentication(final HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        CookieUtils.deleteJwtCookie();
        CookieUtils.deleteRefreshTokenCookie();

        final HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
    }

    /**
     * Vue SPA용 JSON 로그인.
     * DreamdiaryAuthenticationProvider 호출 → 인증 + JWT 쿠키 발급이 함께 처리된다.
     *
     * @param body JSON 바디 {@link LoginRequest}
     * @return {@link ResponseEntity} 처리 결과와 메시지
     */
    @PostMapping(Url.API_AUTH_LOGIN)
    @PermitAll
    @ResponseBody
    public ResponseEntity<AjaxResponse> loginApiAjax(
            final @RequestBody LoginRequest body,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        try {
            final UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(body.getUsername(), body.getPassword());
            final Authentication auth = authenticationProvider.authenticate(token);
            final AuthInfo authInfo = (AuthInfo) auth.getPrincipal();
            this.handleJsonLoginSuccess(request, response, authInfo);
            return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withObj(AuthUserDto.from(authInfo)));
        } catch (final AccountNeedsPwResetException e) {
            final String errorMsg = this.getLoginFailureMsg(e);
            this.publishLoginFailureLog(body.getUsername(), e, errorMsg);
            final Map<String, Object> resetMap = new HashMap<>();
            resetMap.put("username", body.getUsername());
            resetMap.put("needsPasswordReset", true);
            resetMap.put("passwordToken", e.getPasswordToken());
            log.info("Vue login requires password reset. username: {}", body.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AjaxResponse.withAjaxResult(false, errorMsg).withMap(resetMap));
        } catch (final CredentialsExpiredException e) {
            final String errorMsg = this.getLoginFailureMsg(e);
            this.publishLoginFailureLog(body.getUsername(), e, errorMsg);
            final Map<String, Object> expiredMap = new HashMap<>();
            expiredMap.put("username", body.getUsername());
            expiredMap.put("isCredentialExpired", true);
            log.info("Vue login requires password change. username: {} reason: credentialsExpired", body.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AjaxResponse.withAjaxResult(false, errorMsg).withMap(expiredMap));
        } catch (final DupIdLoginException e) {
            final String errorMsg = this.getLoginFailureMsg(e);
            final HttpSession session = request.getSession();
            session.setAttribute("isDupIdLogin", body.getUsername());
            final Map<String, Object> dupLoginMap = new HashMap<>();
            dupLoginMap.put("username", body.getUsername());
            dupLoginMap.put("isDupIdLogin", true);
            log.info("Vue login requires duplicate-login confirmation. username: {}", body.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AjaxResponse.withAjaxResult(false, errorMsg).withMap(dupLoginMap));
        } catch (final BadCredentialsException e) {
            String errorMsg = this.getLoginFailureMsg(e);
            this.publishLoginFailureLog(body.getUsername(), e, errorMsg);
            final AuthPolicyEntity rsAuthPolicyEntity = authPolicyQueryService.getDtlEntity();
            final Integer loginAttemptLimit = rsAuthPolicyEntity.getLoginAttemptLimit();
            final Integer newLoginFailCnt = authService.applyLoginFailCnt(body.getUsername());
            if (newLoginFailCnt < loginAttemptLimit) {
                errorMsg += "<br>" + MessageUtils.getMessage(MessageUtils.LGN_FAIL_BADCREDENTIALS_CNT, new Object[]{loginAttemptLimit, newLoginFailCnt});
            } else {
                authService.lockAccount(body.getUsername());
                errorMsg += "<br>" + MessageUtils.getMessage(MessageUtils.LGN_FAIL_BADCREDENTIALS_LOCKED, new Object[]{newLoginFailCnt});
            }
            log.info("Vue login bad credentials. username: {} failCnt: {}", body.getUsername(), newLoginFailCnt);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AjaxResponse.withAjaxResult(false, errorMsg));
        } catch (final AccountDormantException e) {
            final String errorMsg = this.getLoginFailureMsg(e);
            this.publishLoginFailureLog(body.getUsername(), e, errorMsg);
            authService.lockAccount(body.getUsername());
            log.info("Vue login dormant account locked. username: {}", body.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AjaxResponse.withAjaxResult(false, errorMsg));
        } catch (final AuthenticationException e) {
            final String errorMsg = this.getLoginFailureMsg(e);
            this.publishLoginFailureLog(body.getUsername(), e, errorMsg);
            log.warn("Vue login failed. username: {} errorMsg: {}", body.getUsername(), errorMsg);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AjaxResponse.withAjaxResult(false, errorMsg));
        } catch (final Exception e) {
            log.error("Vue login internal error. username={}", body.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AjaxResponse.withAjaxResult(false, MessageUtils.getMessage("msg.auth.login-processing-error")));
        }
    }

    /**
     * Vue SPA용 JSON 로그아웃.
     * JWT 쿠키 + 리프레시 토큰 쿠키를 삭제하고 세션을 무효화한다.
     *
     * @param request HTTP 요청 객체
     * @return {@link ResponseEntity} 처리 결과와 메시지
     */
    @PostMapping(Url.API_AUTH_LGOUT_JSON)
    @PermitAll
    @ResponseBody
    public ResponseEntity<AjaxResponse> logoutApiAjax(
            final HttpServletRequest request
    ) {
        SecurityContextHolder.clearContext();
        CookieUtils.deleteJwtCookie();
        CookieUtils.deleteRefreshTokenCookie();
        final HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS));
    }

    /**
     * Vue SPA 로그인 요청 바디.
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        private String username;
        private String password;
    }

    /**
     * Vue JSON 로그인 성공 후 Spring form-login 성공 핸들러와 동일한 부수효과를 적용한다.
     *
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param authInfo 인증 사용자 정보
     */
    private void handleJsonLoginSuccess(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final AuthInfo authInfo
    ) {
        request.removeAttribute(Constant.ERROR_MSG);
        request.removeAttribute("isCredentialExpired");
        request.removeAttribute("isDupIdLogin");
        request.removeAttribute("needsPasswordReset");

        authInfo.nullifyPasswordInfo();
        final HttpSession session = request.getSession();
        authSessionPolicyService.applyToSession(session);
        session.setAttribute("authInfo", authInfo);
        session.setAttribute("remoteIp", AuthUtils.getRemoteIpAddr());

        final String username = authInfo.getUsername();
        authService.setLastLoginAt(username);
        DupIdLoginManager.addKey(username);

        publisher.publishAsyncEvent(new LogEvent(this, new LogParam(true, MessageUtils.RSLT_SUCCESS, ActvtyCtgr.LGN)));
        publisher.publishAsyncEvent(new LoginSuccessCacheWarmupEvent(this, username));
        HttpUtils.setInvalidateBrowserCacheHeader(response);
        log.info("Vue login succeeded. username: {}", username);
    }

    /**
     * 로그인 실패 로그를 저장한다.
     *
     * @param username 로그인 시도 계정명
     * @param exception 인증 실패 예외
     * @param errorMsg 사용자에게 반환할 오류 메시지
     */
    private void publishLoginFailureLog(
            final String username,
            final AuthenticationException exception,
            final String errorMsg
    ) {
        if (exception instanceof InternalAuthenticationServiceException || exception instanceof DupIdLoginException) return;
        publisher.publishAsyncEvent(new LogAnonymousEvent(this, new LogParam(username, false, errorMsg, ActvtyCtgr.LGN)));
    }

    /**
     * Spring Security Exception 이름 또는 내부 메시지로 로그인 실패 메시지를 반환한다.
     *
     * @param e 예외 객체
     * @return {@link String} -- 예외에 해당하는 에러 메시지
     */
    private String getLoginFailureMsg(final Exception e) {
        if (e instanceof InternalAuthenticationServiceException) {
            final InternalAuthenticationServiceException iae = (InternalAuthenticationServiceException) e;
            if (StringUtils.isNotBlank(iae.getMessage())) {
                return iae.getMessage();
            }
            final Throwable root = ExceptionUtils.getRootCause(e);
            if (root != null && root != e && StringUtils.isNotBlank(root.getMessage())) {
                return MessageUtils.getExceptionMsg(root);
            }
        }
        final String fullExceptionNm = e.getClass().toString();
        final String exceptionNm = fullExceptionNm.substring(fullExceptionNm.lastIndexOf('.') + 1);
        return MessageUtils.getMessage("AbstractUserDetailsAuthenticationProvider." + exceptionNm);
    }
}
