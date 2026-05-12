package io.nicheblog.dreamdiary.auth.security.config;

import io.nicheblog.dreamdiary.global.TestConstant;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * TestAuditConfig
 * <pre>
 *  테스트용 auditor 정보 세팅 설정
 * </pre>
 *
 * @author nichefish
 */
@TestConfiguration("auditConfig")
@Profile("test")
@Primary
@EnableJpaAuditing(auditorAwareRef = "auditorRef", modifyOnCreate = false)
public class TestAuditConfig {

    /**
     * 테스트용 auditor 세팅
     */
    @Bean
    @Profile("test")
    @Primary // (테스트시) 우선적으로 사용할 빈으로 설정
    public AuditorAware<String> auditorRef() {
        return new TestAuditConfig.AuditAwareImpl();
    }

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
            return Optional.of(TestConstant.TEST_AUDITOR); // 테스트 사용자 이름
        }
    }
}