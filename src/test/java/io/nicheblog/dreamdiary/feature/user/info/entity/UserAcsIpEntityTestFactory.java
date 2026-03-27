package io.nicheblog.dreamdiary.feature.user.info.entity;

import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * UserAcsIpEntityTestFactory
 * <pre>
 *  사용자 접속 IP 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class UserAcsIpEntityTestFactory {

    /**
     * 테스트용 사용자 접속 IP Entity 생성
     */
    public static UserAcsIpEntity create(final String acsIp) {
        return UserAcsIpEntity.builder()
                .acsIp(acsIp)
                .build();
    }
}
