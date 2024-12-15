package io.nicheblog.dreamdiary.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * PasswordEncoderConfig
 * <pre>
 *  패스워드 인코더 Config.
 *  (기존의 WebSecurityConfig에 있을 때 의존성 문제 때문에 별도 클래스로 분리.)
 * </pre>
 *
 * @author nichefish
 */
@Configuration
public class PasswordEncoderConfig {

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
