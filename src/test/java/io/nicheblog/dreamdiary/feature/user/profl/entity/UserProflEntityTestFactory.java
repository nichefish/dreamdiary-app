package io.nicheblog.dreamdiary.feature.user.profl.entity;

import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * UserProflEntityTestFactory
 * <pre>
 *  사용자 프로필 정보 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class UserProflEntityTestFactory {

    /**
     * 테스트용 사용자 프로필 정보 Entity 객체 생성
     */
    public static UserProflEntity create() throws Exception {
        // 객체 생성
        return UserProflEntity.builder()
                .brthdy(DateUtils.asDate("2000-01-01"))
                .proflCn("test_profl_cn")
                .build();
    }
}
