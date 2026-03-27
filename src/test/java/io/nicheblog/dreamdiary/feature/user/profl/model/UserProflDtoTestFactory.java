package io.nicheblog.dreamdiary.feature.user.profl.model;

import io.nicheblog.dreamdiary.feature.user.info.model.profl.UserProflDto;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * UserTestUtils
 * <pre>
 *  사용자 프로필 정보 테스트 Dto 생성 팩토리 모듈
 * </pre>
 * 
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class UserProflDtoTestFactory {

    /**
     * 테스트용 사용자 신청 프로필 정보 Dto 객체 생성 :: 메소드 분리
     */
    public static UserProflDto create() {
        // 객체 생성
        return UserProflDto.builder()
                .brthdy("2000-01-01")
                .proflCn("test_profl_cn")
                .build();
    }
}
