package io.nicheblog.dreamdiary.auth.oauth2.provider;

import io.nicheblog.dreamdiary.auth.jwt.provider.JwtTokenProvider;
import io.nicheblog.dreamdiary.auth.security.exception.AuthenticationFailureException;
import io.nicheblog.dreamdiary.auth.security.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.security.provider.helper.AuthenticationHelper;
import io.nicheblog.dreamdiary.auth.security.service.AuthService;
import io.nicheblog.dreamdiary.global.util.CookieUtils;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * OAuth2Provider
 * <pre>
 *  OAuth2 인증 처리 Provider.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class OAuth2Provider {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationHelper authenticationHelper;

    /**
     * OAuth2 :: 사용자 인증 과정
     *
     * @param authentication 인증 정보를 담고 있는 Authentication 객체
     * @return {@link AuthInfo} -- 인증된 사용자의 Authentication 객체
     * @throws AuthenticationException 처리 중 발생할 수 있는 예외
     */
    @SneakyThrows
    public AuthInfo authenticate(final OAuth2AuthenticationToken authentication) throws AuthenticationException {

        final String email = authentication.getPrincipal().getAttribute("email");
        if (StringUtils.isEmpty(email)) throw new SecurityException(MessageUtils.getMessage("user.email.invalid"));

        final AuthInfo authInfo = authService.loadUserByEmail(email);

        // 인증 객체 생성
        final Boolean isValidated = authenticationHelper.validateAuth(authInfo);
        if (!isValidated) throw new AuthenticationFailureException(MessageUtils.getExceptionMsg("AuthenticationFailureException"));
        final UsernamePasswordAuthenticationToken authToken = authInfo.getAuthToken();

        // 인증 객체를 기반으로 JWT 생성, 임시로 세션에 저장
        final String jwt = this.authenticateAndGenerateJwt(authToken);
        // 세션에 JWT 저장
        final ServletRequestAttributes servletRequestAttribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        final HttpSession session = servletRequestAttribute.getRequest().getSession();
        session.setAttribute("jwt", jwt);
        // HTTP 쿠키에 JWT 저장
        CookieUtils.setJwtCookie(jwt); // 7일간 유지
        // HttpServletResponse 응답 헤더에 JWT 세팅
        final HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
        if (response != null) response.setHeader("Authorization", "Bearer " + jwt);

        // spring security context에 인증 정보 저장
        SecurityContextHolder.getContext().setAuthentication(new OAuth2AuthenticationToken(authInfo, authInfo.getAuthorities(), authentication.getAuthorizedClientRegistrationId()));

        return (AuthInfo) authToken.getPrincipal();
    }

    /**
     * 인증 결과인 {@link Authentication} 객체를 사용하여 JWT 토큰을 생성합니다.
     *
     * @param authentication 인증 정보를 담고 있는 {@link Authentication} 객체.
     * @return {@link String} -- 생성된 JWT 토큰 문자열. 클라이언트에서 인증을 위한 목적으로 사용됩니다.
     */
    public String authenticateAndGenerateJwt(final Authentication authentication) {
        final String username = authentication.getName();
        final List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return jwtTokenProvider.createToken(username, roles); // JWT 생성
    }
}
