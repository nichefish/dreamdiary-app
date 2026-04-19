package io.nicheblog.dreamdiary.auth.policy.entity;

import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * AuthPolicyEntityTestFactory
 * <pre>
 *   인증 정책 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class AuthPolicyEntityTestFactory {

    /**
     * 테스트용 인증 정책 Entity 생성
     */
    public static AuthPolicyEntity create() throws Exception {
        return AuthPolicyEntity.builder()
                .loginAttemptLimit(5)
                .loginAttemptWindowMinutes(10)
                .accountLockDurationMinutes(30)
                .passwordChangeCycleDays(30)
                .inactiveLockDays(30)
                .passwordResetTokenExpiryMinutes(30)
                .build();
    }
}
