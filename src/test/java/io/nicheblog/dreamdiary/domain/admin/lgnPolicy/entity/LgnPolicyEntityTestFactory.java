package io.nicheblog.dreamdiary.domain.admin.lgnPolicy.entity;

import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * LgnPolicyEntityTestFactory
 * <pre>
 *   로그인 정책 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class LgnPolicyEntityTestFactory {

    /**
     * 테스트용 로그인 정책 Entity 생성
     */
    public static LgnPolicyEntity create() throws Exception {
        return LgnPolicyEntity.builder()
                .lgnLockDy(30)
                .lgnTryLmt(5)
                .pwChgDy(30)
                .pwForReset("test_password")
                .build();
    }
}
