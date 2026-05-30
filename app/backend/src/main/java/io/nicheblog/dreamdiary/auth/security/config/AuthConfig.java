package io.nicheblog.dreamdiary.auth.security.config;

import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.session.HttpSessionEventPublisher;

/**
 * SecurityConfig
 * <pre>
 *  Spring Security 설정
 * </pre>
 *
 * @author nichefish
 */
@Configuration
public class AuthConfig {

    /**
     * 빈 등록 ::중복 로그인 방지:: logout 후 login 처리시 정상작동을 위함
     *
     * @return {@link SessionRegistry} -- SessionRegistry 객체
     */
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    /**
     * 빈 등록 :: 중복 로그인 방지:: WAS가 여러 개 있을 때 처리 (session clustering)
     *
     * @return {@link ServletListenerRegistrationBean} -- ServletListenerRegistrationBean 객체
     */
    @Bean
    public static ServletListenerRegistrationBean<?> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
    }

    /**
     * 빈 등록 :: PasswordEncoder.
     *
     * @return {@link PasswordEncoder} -- PasswordEncoder 객체
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // 혹은 다른 인코더
    }
}
