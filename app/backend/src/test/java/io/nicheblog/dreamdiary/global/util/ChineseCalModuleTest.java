package io.nicheblog.dreamdiary.global.util;

import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


/**
 * ChineseCalUtilsTest
 * <pre>
 *  ChineseCalUtils(음력/양력 관련 처리 모듈) 테스트 모듈
 * </pre>
 *
 * @author nichefish
 */
@Log4j2
public class ChineseCalModuleTest {

    @Test
    @DisplayName("양력 -> 음력 날짜변환 테스트")
    public void todaySolarToLunarTest() throws Exception {

        /* Given */
        String todaySolarStr = DateUtils.getCurrDateStr(DatePtn.DATE);

        /* When */
        String todayLunarStr = DateUtils.ChineseCal.solToLunStr(todaySolarStr, DatePtn.DATE);
        log.info("todayStr: {}, todayLunarStr: {}", todaySolarStr, todayLunarStr);

        /* then */
        assert true;
    }

    @Test
    @DisplayName("음력 -> 양력 날짜변환 테스트")
    public void todayLunarToSolarTest() throws Exception {

        /* Given */
        String todayLunarStr = DateUtils.getCurrDateStr(DatePtn.DATE);

        /* When */
        String todaySolarStr = DateUtils.ChineseCal.lunToSolStr(todayLunarStr, DatePtn.DATE);
        log.info("todayLunarStr: {}, todaySolarStr: {}", todayLunarStr, todaySolarStr);

        /* then */
        assert true;
    }
}