package io.nicheblog.dreamdiary.feature.user.emplym.model;

import io.nicheblog.dreamdiary.feature.user.info.model.emplym.UserEmplymDto;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * UserEmplymDtoTestFactory
 * <pre>
 *  사용자 인사정보 테스트 Dto 생성 팩토리 모듈
 * </pre>
 * 
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class UserEmplymDtoTestFactory {

    /**
     * 테스트용 사용자 신청 인사정보 Dto 객체 생성 :: 메소드 분리
     */
    public static UserEmplymDto create() {
        // 객체 생성
        return UserEmplymDto.builder()
                .userNm("1987-03-11")
                .cmpyCd("test_cmpy_cd")
                .teamCd("test_team_cd")
                .emplymCd("test_emplym_cd")
                .rankCd("test_rank")
                .apntcYn("Y")
                .emplymEmailId("test_emplym_email_id")
                .emplymEmailDomain("test_emplym_email_domain")
                .emplymCttpc("000-0000-0000")
                .ecnyDt("2000-01-01")
                .retireYn("Y")
                .retireDt("2000-01-01")
                .acntBank("test_acnt_bank")
                .acntNo("test-acnt_no")
                .emplymCn("test_emplym_cn")
                .build();
    }

}
