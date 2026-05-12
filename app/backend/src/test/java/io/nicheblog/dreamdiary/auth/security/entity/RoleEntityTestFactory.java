package io.nicheblog.dreamdiary.auth.security.entity;

import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * RoleEntityTestFactory
 * <pre>
 *  권한 정보 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class RoleEntityTestFactory {

    /**
     * 테스트용 조치자 Entity 생성
     */
    public static RoleEntity create() throws Exception {
        return RoleEntity.builder()
                .roleKey("TEST_AUTH_CD")
                .roleName("테스트 권한")
                .authLevel(1)
                .sortOrder(1)
                .useYn("Y")
                .build();
    }
}
