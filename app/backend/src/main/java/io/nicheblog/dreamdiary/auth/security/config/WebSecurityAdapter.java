package io.nicheblog.dreamdiary.auth.security.config;

import io.nicheblog.dreamdiary.auth.config.AuthProperties;
import io.nicheblog.dreamdiary.auth.jwt.filter.JwtAuthenticationFilter;
import io.nicheblog.dreamdiary.auth.oauth2.handler.OAuth2AuthenticationFailureHandler;
import io.nicheblog.dreamdiary.auth.oauth2.handler.OAuth2AuthenticationSuccessHandler;
import io.nicheblog.dreamdiary.auth.policy.model.AuthPolicyQueryDto;
import io.nicheblog.dreamdiary.auth.policy.service.AuthPolicyQueryService;
import io.nicheblog.dreamdiary.auth.security.handler.AjaxAwareAuthenticationEntryPoint;
import io.nicheblog.dreamdiary.auth.security.handler.LoginFailureHandler;
import io.nicheblog.dreamdiary.auth.security.handler.LoginSuccessHandler;
import io.nicheblog.dreamdiary.auth.security.handler.LogoutHandler;
import io.nicheblog.dreamdiary.auth.security.service.AuthService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.time.Duration;

/**
 * WebSecurityAdapter
 * <pre>
 *  Spring Security 전체 진입점.
 *  웹사이트 URL 경로에 대한 인증을 설정한다.
 * </pre>
 *
 * @author nichefish
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@RequiredArgsConstructor
@Log4j2
public class WebSecurityAdapter {

    private final AuthService authService;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService;
    private final LoginFailureHandler webLoginFailureHandler;
    private final LoginSuccessHandler webLoginSuccessHandler;
    private final OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oauth2AuthenticationFailureHandler;
    private final AjaxAwareAuthenticationEntryPoint ajaxAwareAuthenticationEntryPoint;
    private final LogoutHandler logoutHandler;
    private final AuthenticationProvider authenticationProvider;
    private final SessionRegistry sessionRegistry;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthPolicyQueryService authPolicyQueryService;
    private final AuthProperties authProperties;

    @Value("${springdoc.api-docs.path:/v3/api-docs}")
    private String API_DOCS_PATH;
    @Value("${springdoc.swagger-ui.path:/swagger-ui.html}")
    private String SWAGGER_UI_PATH;

    /**
     * Security 필터 자체를 타지 않도록 제외할 경로 정의
     * 여기 등록된 경로는 Spring Security FilterChain 자체를 통과하지 않음. 즉, 인증/인가/예외처리 로직이 아예 적용되지 않는다.
     * 사용 기준:
     * - 완전 public 자원 (static, 에러페이지, 회원가입 등)
     * - 인증 필요성이 절대 없는 endpoint
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {

        return (web) -> web.ignoring()
                // 세션 만료 처리 URL
                .antMatchers(Url.API_AUTH_EXPIRE_SESSION)
                // static 디렉터리의 하위 파일 목록은 인증 무시(=항상 통과 )
                .antMatchers("/favicon.ico")
                .antMatchers("/robots.txt")
                // 에러 페이지
                .antMatchers(Url.ERROR + "/**")
                // 비밀번호 만료시 비밀번호 변경 화면
                .antMatchers(Url.API_AUTH_LGN_PW_CHG)
                // 신규계정 신청 화면/기능 전체 접근 (+아이디 중복 체크)
                // 계정 신청 등록 API는 관리자 목록 조회와 같은 URL을 쓰므로 SecurityContext 생성을 위해 FilterChain을 통과한다.
                // 비로그인 POST 접근은 아래 authorizeRequests()의 /api/** permitAll 계약으로 유지한다.
                .antMatchers(Url.USER_SIGNUP_PAGE)
                .antMatchers(Url.USERS_DUPLICATE_USERNAME_CHECK)
                .antMatchers(Url.USERS_DUPLICATE_EMAIL_CHECK);
    }

    /**
     * HTTP 보안 설정을 구성합니다.
     *
     * @param http HttpSecurity 객체로, HTTP 보안 관련 설정을 구성하는 데 사용됩니다
     */
    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {

        // 인증 Provider 등록
        // → Form Login 시 사용자 인증 로직 위임 대상
        http.authenticationProvider(authenticationProvider);

        // Form 로그인 설정
        http.formLogin()
                .loginPage(Url.VUE_SIGN_IN)
                .usernameParameter("username")
                .passwordParameter("password")
                .loginProcessingUrl(Url.API_AUTH_LGN_PROC)
                .defaultSuccessUrl(Url.MAIN)
                .failureHandler(webLoginFailureHandler)
                .successHandler(webLoginSuccessHandler)
                .permitAll();

        // JWT 인증 필터 추가 (요청마다 토큰 검증 → 인증 객체 세팅)
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // OAuth2 로그인 설정 (외부 인증 (구글 등) 이후 사용자 정보 로드)
        http.oauth2Login()
                .loginPage(Url.VUE_SIGN_IN)
                .userInfoEndpoint()
                    .userService(oauth2UserService)
                .and()
                .successHandler(oauth2AuthenticationSuccessHandler)
                .failureHandler(oauth2AuthenticationFailureHandler);

        // URL 접근 제어 (인가 정책) - 위에서부터 순서대로 매칭됨
        http.authorizeRequests()
                // static resource 전체 접근
                .antMatchers(Constant.STATIC_PATHS)
                .permitAll()
                // API 접근 전체 허용 (현재는 인증 적용하지 않음)
                // TODO: inbound API 쪽에 토큰 인증 적용하기
                .antMatchers("/api/**")
                .permitAll()
                // OAUTH2 인증 관련 페이지
                .antMatchers("/oauth2/authorization/**", "/login/oauth2/code/**")
                .permitAll()
                // Swagger/OpenAPI docs and UI resources
                .antMatchers(API_DOCS_PATH, API_DOCS_PATH + "/**", SWAGGER_UI_PATH, "/swagger-ui/**")
                .permitAll()
                // WebSocket 엔드포인트에 대한 접근 허용
                .antMatchers("/chat/**")
                .permitAll()
                // 로그인 화면 i18n catalog json
                // 변경 전: /i18n/** 경로는 anyRequest().authenticated()에 걸려 401 가능
                // 변경 후: /i18n/** 경로는 비인증 접근 허용
                .antMatchers("/i18n/**")
                .permitAll()
                // 이외 페이지 = 로그인 사용자만 접근
                .anyRequest()
                .authenticated();

        // CSRF 비활성화
        // - REST + JWT 환경에서는 보통 disable. Form 기반만 사용할 경우는 재고려 필요
        http.csrf()
                .disable();

        // remember-me 관련 (쿠키 기반 장기 로그인)
        http.rememberMe()
                .key(authProperties.getRememberMe().getKey())
                .rememberMeParameter(authProperties.getRememberMe().getParam())
                .tokenValiditySeconds((int) Duration.ofDays(authProperties.getRememberMe().getTokenTtlDays()).getSeconds())
                .userDetailsService(authService)
                .authenticationSuccessHandler(webLoginSuccessHandler);

        // 중복 로그인 정책
        final SessionManagementConfigurer<HttpSecurity> sessionManagement = http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
        if (this.isDuplicateLoginAllowed()) {
            log.info("Security session policy initialized. duplicateLoginAllowed=true");
        } else {
            sessionManagement
                    .maximumSessions(1)
                    .maxSessionsPreventsLogin(false)
                    .expiredUrl(Url.VUE_SIGN_IN + "?dupLoginAt=Y")
                    .sessionRegistry(sessionRegistry);
            log.info("Security session policy initialized. duplicateLoginAllowed=false");
        }

        // 로그아웃 설정
        http.logout()
                .logoutRequestMatcher(new AntPathRequestMatcher(Url.API_AUTH_LGOUT))
                .logoutUrl(Url.API_AUTH_LGOUT)
                .logoutSuccessUrl(Url.VUE_SIGN_IN)
                .addLogoutHandler(logoutHandler)
                .invalidateHttpSession(true);

        // 401/403 예외처리 핸들링
        http.exceptionHandling()
                .authenticationEntryPoint(ajaxAwareAuthenticationEntryPoint)
                .accessDeniedPage(Url.ERROR_ACCESS_DENIED);

        return http.build();
    }

    private boolean isDuplicateLoginAllowed() {
        try {
            final AuthPolicyQueryDto authPolicy = authPolicyQueryService.getDtlDto();
            return authPolicy != null && "Y".equalsIgnoreCase(authPolicy.getDuplicateLoginAllowedYn());
        } catch (final Exception e) {
            log.warn("Failed to read duplicate login policy from auth_policy. fallback=denyDuplicateLogin", e);
            return false;
        }
    }

}
