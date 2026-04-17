package io.nicheblog.dreamdiary.feature.user.emplym.entity;

import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * UserEmplymEntityTestFactory
 * <pre>
 *  사용자 인사정보 테스트 Entity 생성 팩토리 모듈
 * </pre>
 * 
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class UserEmplymEntityTestFactory {

    /**
     * 테스트용 사용자 인사정보 Entity 객체 생성
     */
    public static UserEmplymEntity create() throws Exception {
        // 객체 생성
        return UserEmplymEntity.builder()
                .userNm(TestConstant.TEST_USER)
                .cmpyCd("test_cmpy_cd")
                .teamCd("test_team_cd")
                .emplymCd("test_emplym_cd")
                .rankCd("test_rank")
                .apntcYn("Y")
                .emplymEmail("test_emplym_email_id@test_emplym_email_domain")
                .emplymPhoneNumber("000-0000-0000")
                .ecnyDt(DateUtils.asDate("2000-01-01"))
                .retireYn("Y")
                .retireDt(DateUtils.asDate("2000-01-01"))
                .acntBank("test_acnt_bank")
                .acntNo("test-acnt_no")
                .emplymCn("test_emplym_cn")
                .build();
    }

}
