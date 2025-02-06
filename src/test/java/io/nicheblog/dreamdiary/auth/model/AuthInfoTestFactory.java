package io.nicheblog.dreamdiary.auth.model;

import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

/**
 * AuthInfoTestFactory
 * <pre>
 *  인증정보(AuthInfo) 테스트 Dto 생성 팩토리 모듈
 * </pre>
 * 
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class AuthInfoTestFactory {

    /**
     * 테스트용 인증정보 Dto 객체 생성 :: 메소드 분리
     */
    public static AuthInfo createAuthInfo() {
        // 객체 생성
        return AuthInfo.builder()
                .authList(List.of(AuthRoleDto.builder().build()))
                .build();
    }

}
