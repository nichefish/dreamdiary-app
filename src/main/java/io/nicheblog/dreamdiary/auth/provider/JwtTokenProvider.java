package io.nicheblog.dreamdiary.auth.provider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.nicheblog.dreamdiary.auth.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.service.AuthService;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * JwtTokenProvider
 * <pre>
 *  Spring Security :: 로그인 및 인증 처리.
 *  비밀번호 체크 + 접속IP + 비밀번호 변경기간 체크기능 추가하여 구현
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JwtTokenProvider {

    private final AuthService authService;
    private final AuthenticationHelper authenticationHelper;

    @Value("${spring.jwt.secret}")
    private String secretKey;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * JWT :: 사용자 인증 과정
     *
     * @param token 인증 정보를 담고 있는 Authentication 객체
     * @return {@link AuthInfo} -- 인증된 사용자의 Authentication 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    public AuthInfo authenticate(final String token) throws Exception {
        Authentication authentication = this.getAuthentication(token);
        // spring security context에 인증 정보 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return (AuthInfo) authentication.getPrincipal();
    }

    /**
     * JWT 토큰을 생성합니다.
     *
     * @param userId 사용자 ID
     * @param roles 사용자 권한 목록
     * @return {@link String} -- 생성된 JWT 토큰 문자열
     */
    public String createToken(final String userId, final List<String> roles) {
        final Claims claims = Jwts.claims().setSubject(userId);
        claims.put("roles", roles);
        final Date now = DateUtils.getCurrDate();
        
        final long tokenValidMillisecond = 1000L * 60 * 60;     // 1시간
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + tokenValidMillisecond))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /**
     * 주어진 JWT 토큰에서 사용자 인증 정보를 가져옵니다.
     *
     * @param token JWT 토큰
     * @return {@link Authentication} -- 사용자 인증 정보
     * @throws Exception 인증 과정에서 발생할 수 있는 예외
     */
    public Authentication getDirectAuthentication(final String token) throws Exception {
        final String username = this.getUsernameFromToken(token);
        final AuthInfo authInfo = authService.loadUserByUsername(username);
        authInfo.nullifyPasswordInfo();

        return new UsernamePasswordAuthenticationToken(authInfo, null, authInfo.getAuthorities());
    }

    /**
     * 주어진 JWT 토큰에서 사용자 인증 정보를 가져옵니다.
     *
     * @param token JWT 토큰
     * @return {@link Authentication} -- 사용자 인증 정보
     * @throws Exception 인증 과정에서 발생할 수 있는 예외
     */
    public Authentication getAuthentication(final String token) throws Exception {
        final String username = this.getUsernameFromToken(token);
        final AuthInfo authInfo = authService.loadUserByUsername(username);

        authenticationHelper.doAuth(authInfo);
        return new UsernamePasswordAuthenticationToken(authInfo, null, authInfo.getAuthorities());
    }

    /**
     * JWT 토큰에서 사용자 이름을 추출합니다.
     *
     * @param token JWT 토큰
     * @return {@link String} -- 사용자 이름
     */
    public String getUsernameFromToken(final String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * 요청에서 JWT 토큰을 추출합니다.
     *
     * @param request HTTP 요청 객체 (MVC)
     * @return {@link String} -- 추출된 JWT 토큰 문자열
     */
    public String resolveToken(final HttpServletRequest request) {
        // 쿠키에서 JWT 토큰 추출
        final Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (final Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) return cookie.getValue();  // JWT 토큰 반환
            }
        }
        // 쿠키에 없을 시 :: 헤더에서 JWT 토큰 추출
        final String authTokenStr = request.getHeader("Authorization");
        if (StringUtils.isNotEmpty(authTokenStr) && authTokenStr.startsWith("Bearer ")) {
            return authTokenStr.replace("Bearer ", "");
        }

        return request.getHeader("X-AUTH-TOKEN");
    }

    /**
     * 요청에서 JWT 토큰을 추출합니다.
     *
     * @param request HTTP 요청 객체 (WebFlux)
     * @return {@link String} -- 추출된 JWT 토큰 문자열
     */
    public String resolveToken(final ServerHttpRequest request) {
        // 헤더에서 JWT 토큰 추출
        final HttpHeaders headers = request.getHeaders();
        final String authTokenStr = headers.getFirst("Authorization");
        if (StringUtils.isNotEmpty(authTokenStr) && authTokenStr.startsWith("Bearer ")) {
            return authTokenStr.replace("Bearer ", "");
        }

        return request.getHeaders().getFirst("X-AUTH-TOKEN");
    }

    /**
     * 주어진 JWT 토큰의 유효성을 검증합니다.
     *
     * @param token JWT 토큰
     * @return {@link Boolean} -- 유효한 경우 true, 그렇지 않은 경우 false
     */
    public Boolean validateToken(final String token) {
        try {
            final Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);

            return !claims.getBody().getExpiration().before(new Date());
        } catch (final Exception e) {
            return false;
        }
    }
}
