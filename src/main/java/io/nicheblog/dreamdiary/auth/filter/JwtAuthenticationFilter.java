package io.nicheblog.dreamdiary.auth.filter;

import io.nicheblog.dreamdiary.auth.config.WebSecurityConfig;
import io.nicheblog.dreamdiary.auth.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.provider.JwtTokenProvider;
import io.nicheblog.dreamdiary.auth.util.AuthUtils;
import io.nicheblog.dreamdiary.global.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * JwtAuthenticationFilter
 * <pre>
 *  HTTP 요청에서 JWT를 추출하고, 유효성을 검사하여 {@link SecurityContextHolder}에 인증 정보를 설정합니다.
 * </pre>
 *
 * @author nichefish
 * @see WebSecurityConfig
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * JWT 인증을 위한 필터입니다.
     *
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param filterChain 필터 체인 객체, 다음 필터로 요청을 전달하는 데 사용됩니다
     * @throws ServletException 요청 처리 중 발생하는 서블릿 예외
     * @throws IOException 입출력 처리 중 발생하는 예외
     */
    @Override
    protected void doFilterInternal(
            final @NotNull HttpServletRequest request,
            final @NotNull HttpServletResponse response,
            final @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (AuthUtils.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        //  정적 리소스 요청인 경우 필터 체인을 바로 통과시킴
        String requestUri = request.getRequestURI();
        for (String path : Constant.STATIC_PATHS) {
            if (requestUri.startsWith(path.replace("/**", "/"))) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        String token = jwtTokenProvider.resolveToken(request);
        // 토큰이 없거나 유효하지 않으면 필터 체인을 바로 통과시킴
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 토큰에서 인증 정보 추출
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            // spring security context에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
            AuthInfo authInfo = (AuthInfo) authentication.getPrincipal();
            // 세션에 authInfo 저장
            final ServletRequestAttributes servletRequestAttribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            final HttpSession session = servletRequestAttribute.getRequest().getSession();
            session.setAttribute("authInfo", authInfo);
        } catch (AuthenticationException e) {
            log.error("Authentication failed: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed: " + e.getMessage());
            return; // 필터 체인 중단
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
            return; // 필터 체인 중단
        }

        filterChain.doFilter(request, response);
    }
}