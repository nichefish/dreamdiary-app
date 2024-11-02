package io.nicheblog.dreamdiary.domain.user.info.service;

import io.nicheblog.dreamdiary.domain.user.info.model.UserDto;
import io.nicheblog.dreamdiary.global.config.TestAuditConfig;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserServiceTest
 * 사용자 관리 > 계정 및 권한 관리 서비스 모듈 테스트
 * @author nichefish
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestAuditConfig.class)
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("(음력) 생일인 직원 조회 테스트")
    public void getBrthdyCrdtUserTest() throws Exception {

        UserDto user = userService.getDtlDto("nichefish");

        /* Given */
        // TODO: autowired 문제 해결하기?
        String todayStrA = "2023-01-25";
        String todayStrB = "2023-02-15";
        String brthdy = "1991-01-25";
        String lunarYn = "Y";

        String todayLunarStr, todayLunarYear, brthMnthDy, thisLunarBrthdy;

        /* When: case A */
        // 음력일 경우 = 1) 오늘 날짜를 음력 날짜로 변환 후 2) 아래 로직 그대로 적용. (봐야한다)
        todayLunarStr = DateUtils.ChineseCal.solToLunStr(todayStrA, DatePtn.DATE);
        todayLunarYear = todayLunarStr.substring(0, 4);

        brthMnthDy = brthdy.substring(4);
        thisLunarBrthdy = todayLunarYear + brthMnthDy;

        /* then */
        assert !todayLunarStr.equals(thisLunarBrthdy);

        /* When: case B */
        // 음력일 경우 = 1) 오늘 날짜를 음력 날짜로 변환 후 2) 아래 로직 그대로 적용. (봐야한다)
        todayLunarStr = DateUtils.ChineseCal.solToLunStr(todayStrB, DatePtn.DATE);
        todayLunarYear = todayLunarStr.substring(0, 4);

        brthMnthDy = brthdy.substring(4);
        thisLunarBrthdy = todayLunarYear + brthMnthDy;

        /* then */
        assert todayLunarStr.equals(thisLunarBrthdy);
    }

}