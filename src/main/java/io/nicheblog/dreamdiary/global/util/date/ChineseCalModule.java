package io.nicheblog.dreamdiary.global.util.date;

import com.ibm.icu.util.ChineseCalendar;

import java.util.Calendar;
import java.util.Date;

/**
 * ChineseCalUtils
 * <pre>
 *  음력/양력 관련 처리 유틸리티 모듈
 * </pre>
 *
 * @author nichefish
 */
class ChineseCalModule {

    /**
     * 양력 날짜를 음력 날짜로 변환
     *
     * @param date 변환할 양력 날짜 (`String`, `Date`, `LocalDate` 등 지원)
     * @return {@link Date} 변환된 음력 날짜 (`Date` 객체)
     * @throws Exception 날짜 변환 중 오류가 발생할 경우
     */
    public static Date solToLunar(final Object date) throws Exception {

        final ChineseCalendar chineseCal = new ChineseCalendar();
        final Calendar cal = Calendar.getInstance();

        final String dateStr = DateUtils.asStr(date, DatePtn.DATE);
        final String[] dateStrArr = dateStr.split("-");

        // Calendar에 날짜 세팅
        cal.set(Calendar.YEAR, Integer.parseInt(dateStrArr[0]));
        cal.set(Calendar.MONTH, Integer.parseInt(dateStrArr[1]) - 1);
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateStrArr[2]));

        // ChineseCalendar로 변환 후 날짜 추출
        chineseCal.setTimeInMillis(cal.getTimeInMillis());

        final int yyyy = chineseCal.get(ChineseCalendar.EXTENDED_YEAR) - 2637;
        final int mm = chineseCal.get(ChineseCalendar.MONTH) + 1;
        final int dd = chineseCal.get(ChineseCalendar.DAY_OF_MONTH);

        return DateUtils.asDate(String.format("%04d", yyyy) + String.format("%02d", mm) + String.format("%02d", dd));
    }

    /**
     * 양력 날짜를 음력 날짜 문자열로 변환
     *
     * @param date 변환할 양력 날짜 (`String`, `Date`, `LocalDate` 등 지원)
     * @param ptn 변환된 음력 날짜의 출력 형식을 지정하는 패턴 (`DatePtn`)
     * @return {@link String} 변환된 음력 날짜를 지정된 패턴으로 포맷한 문자열
     * @throws Exception 양력 → 음력 변환 또는 문자열 변환 중 오류가 발생할 경우
     */
    public static String solToLunStr(final Object date, final DatePtn ptn) throws Exception {
        return DateUtils.asStr(solToLunar(date), ptn);
    }

    /**
     * 음력 날짜를 양력 날짜로 변환
     *
     * @param date 변환할 음력 날짜 (`String`, `Date`, `LocalDate` 등 지원)
     * @return {@link Date} 변환된 양력 날짜
     * @throws Exception 날짜 변환 중 오류가 발생할 경우
     */
    public static Date lunToSol(final Object date) throws Exception {

        final ChineseCalendar chineseCal = new ChineseCalendar();
        final Calendar cal = Calendar.getInstance();

        final String dateStr = DateUtils.asStr(date, DatePtn.DATE);
        final String[] dateStrArr = dateStr.split("-");

        // ChineseCalendar에 날짜 세팅
        chineseCal.set(ChineseCalendar.EXTENDED_YEAR, Integer.parseInt(dateStrArr[0]) + 2637);
        chineseCal.set(ChineseCalendar.MONTH, Integer.parseInt(dateStrArr[1]) - 1);
        chineseCal.set(ChineseCalendar.DAY_OF_MONTH, Integer.parseInt(dateStrArr[2]));

        // calendar로 변환 후 날짜 추출
        cal.setTimeInMillis(chineseCal.getTimeInMillis());

        return cal.getTime();
    }

    /**
     * 음력 날짜를 양력 날짜 문자열로 변환
     *
     * @param date 변환할 음력 날짜 (지원되는 타입: `String`, `Date`, `LocalDate` 등)
     * @param ptn 변환된 양력 날짜의 출력 형식을 지정하는 패턴 (`DatePtn`)
     * @return {@link String} 변환된 양력 날짜를 지정된 패턴으로 포맷한 문자열
     */
    public static String lunToSolStr(final Object date, final DatePtn ptn) throws Exception {
        return DateUtils.asStr(lunToSol(date), ptn);
    }
}