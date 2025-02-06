package io.nicheblog.dreamdiary.auth.security.entity;

import io.nicheblog.dreamdiary.auth.security.entity.AuthRoleEntity;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * AuthRoleEntityTestFactory
 * <pre>
 *  권한 정보 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class AuthRoleEntityTestFactory {

    /**
     * 테스트용 조치자 Entity 생성
     */
    public static AuthRoleEntity create() throws Exception {
        return AuthRoleEntity.builder()
                .build();
    }
}
