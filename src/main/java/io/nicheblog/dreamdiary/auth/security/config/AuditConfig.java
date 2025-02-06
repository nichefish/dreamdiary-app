package io.nicheblog.dreamdiary.auth.security.config;

import io.nicheblog.dreamdiary.auth.security.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.global.Constant;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;

import java.util.Optional;

/**
 * AuditConfig
 * <pre>
 *  JpaAudit 설정 (등록/수정자, 등록/수정일시 자동화)
 * </pre>
 *
 * @author nichefish
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorRef", modifyOnCreate = false)
public class AuditConfig {

    /**
     * 빈 등록 :: auditorRef. (현재 작업자 감지)
     *
     * @return {@link AuditAwareImpl} -- AuditAwareImpl 객체
     */
    @Bean
    public AuditAwareImpl auditorRef() {
        return new AuditAwareImpl();
    }

    /* ----- */

    /**
     * AuditAwareImpl
     * <pre>
     *  현재 작업자(Auditor) 정보 조회 방법 세팅
     * </pre>
     *
     * @author nichefish
     */
    public static class AuditAwareImpl
            implements AuditorAware<String> {

        /**
         * 현재 인증(로그인) 상태인 등록/수정자 반환
         */
        @Override
        public @NotNull Optional<String> getCurrentAuditor() {
            final Authentication authentication = AuthUtils.getAuthentication();

            if (!AuthUtils.isAuthenticated()) return Optional.of(Constant.SYSTEM_ACNT);

            // 비로그인"Anonymous"시 시스템 계정(system)으로 자동 처리
            final Object principal = authentication.getPrincipal();
            boolean isAnonymous = (principal instanceof String);
            if (isAnonymous) return Optional.of(Constant.SYSTEM_ACNT);

            // 로그인 사용자 ID 반환
            final AuthInfo authInfo = (AuthInfo) authentication.getPrincipal();
            return Optional.of(authInfo.getUserId());
        }
    }
}
