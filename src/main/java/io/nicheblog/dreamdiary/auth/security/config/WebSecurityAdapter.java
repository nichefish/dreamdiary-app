package io.nicheblog.dreamdiary.auth.security.config;

import io.nicheblog.dreamdiary.auth.jwt.filter.JwtAuthenticationFilter;
import io.nicheblog.dreamdiary.auth.oauth2.handler.OAuth2AuthenticationFailureHandler;
import io.nicheblog.dreamdiary.auth.oauth2.handler.OAuth2AuthenticationSuccessHandler;
import io.nicheblog.dreamdiary.auth.security.handler.AjaxAwareAuthenticationEntryPoint;
import io.nicheblog.dreamdiary.auth.security.handler.LogoutHandler;
import io.nicheblog.dreamdiary.auth.security.handler.LoginFailureHandler;
import io.nicheblog.dreamdiary.auth.security.handler.LoginSuccessHandler;
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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

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
    private final LoginFailureHandler webLgnFailureHandler;
    private final LoginSuccessHandler webLgnSuccessHandler;
    private final OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oauth2AuthenticationFailureHandler;
    private final AjaxAwareAuthenticationEntryPoint ajaxAwareAuthenticationEntryPoint;
    private final LogoutHandler lgoutHandler;
    private final AuthenticationProvider authenticationProvider;
    private final SessionRegistry sessionRegistry;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${springdoc.api-docs.path:/v3/api-docs}")
    private String API_DOCS_PATH;
    @Value("${springdoc.swagger-ui.path:/swagger-ui.html}")
    private String SWAGGER_UI_PATH;
    @Value("${remember-me.key}")
    private String REMEMBER_ME_KEY;
    @Value("${remember-me.param}")
    private String REMEMBER_ME_PARAM;

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
                .antMatchers(Url.USER_REQST_REG_FORM)
                .antMatchers(Url.USER_REQST_REG_AJAX)
                .antMatchers(Url.USERNAME_DUP_CHK_AJAX)
                .antMatchers(Url.USER_EMAIL_DUP_CHK_AJAX);
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
                .loginPage(Url.APP_AUTH_LGN_FORM)
                .usernameParameter("username")
                .passwordParameter("password")
                .loginProcessingUrl(Url.API_AUTH_LGN_PROC)
                .defaultSuccessUrl(Url.MAIN)
                .failureHandler(webLgnFailureHandler)
                .successHandler(webLgnSuccessHandler)
                .permitAll();

        // JWT 인증 필터 추가 (요청마다 토큰 검증 → 인증 객체 세팅)
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // OAuth2 로그인 설정 (외부 인증 (구글 등) 이후 사용자 정보 로드)
        http.oauth2Login()
                .loginPage(Url.APP_AUTH_LGN_FORM)
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
                // 이외 페이지 = 로그인 사용자만 접근
                .anyRequest()
                .authenticated();

        // CSRF 비활성화
        // - REST + JWT 환경에서는 보통 disable. Form 기반만 사용할 경우는 재고려 필요
        http.csrf()
                .disable();

        // remember-me 관련 (쿠키 기반 장기 로그인)
        http.rememberMe()
                .key(REMEMBER_ME_KEY)
                .rememberMeParameter(REMEMBER_ME_PARAM)
                .tokenValiditySeconds(86400 * 30)
                .userDetailsService(authService)
                .authenticationSuccessHandler(webLgnSuccessHandler);

        // 중복 로그인 방지
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)     // 최대 1개
                .maxSessionsPreventsLogin(false)        // true:: 나중에 접속한 사용자 로그인 방지, false:: 먼저 접속한 사용자 로그아웃 처리
                .expiredUrl(Url.APP_AUTH_LGN_FORM + "?dupLgnAt=Y")
                .sessionRegistry(sessionRegistry);

        // 로그아웃 설정
        http.logout()
                .logoutRequestMatcher(new AntPathRequestMatcher(Url.API_AUTH_LGOUT))
                .logoutUrl(Url.API_AUTH_LGOUT)
                .logoutSuccessUrl(Url.APP_AUTH_LGN_FORM)
                .addLogoutHandler(lgoutHandler)
                .invalidateHttpSession(true);

        // 401/403 예외처리 핸들링
        http.exceptionHandling()
                .authenticationEntryPoint(ajaxAwareAuthenticationEntryPoint)
                .accessDeniedPage(Url.ERROR_ACCESS_DENIED);

        return http.build();
    }

}
