package io.nicheblog.dreamdiary.global.util.date;

import io.nicheblog.dreamdiary.global.Constant;
import lombok.experimental.UtilityClass;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * DateUtils
 * <pre>
 *  날짜 연산 관련 로직 처리 유틸리티 모듈.
 *  기존 apache 함수도 다 쓰기 위해 commons.lang3.time.DateUtils 상속
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
public class DateUtils
        extends org.apache.commons.lang3.time.DateUtils {

    /** 날짜 파싱 관련 메소드 위임 */
    public static class Parser extends DateParser {}
    public Parser parser = new Parser();

    /** 음력 관련 메소드 위임 */
    public static class ChineseCal extends ChineseCalModule {}

    public static final String PTN_DATE = "yyyy-MM-dd";
    public static final String PTN_DATETIME = "yyyy-MM-dd HH:mm:ss";

    public static final SimpleDateFormat DF_DATE = new SimpleDateFormat(PTN_DATE, Constant.LC_KO);

    /**
     * 날짜 또는 문자열을 받아서 날짜Date로 일괄 반환
     * (Date, 문자열, LocalDateTime)
     */
    public static Date asDate(final Object date) throws Exception {
        if (date == null) return null;
        if (date instanceof String) {
            final String dateStrParam = date.toString();
            final String dateStr = (dateStrParam.length() > 20) ? dateStrParam.substring(0, 19) : dateStrParam;
            return Parser.strToDate(dateStr);
        }
        if (date instanceof Date) return (Date) date;
        if (date instanceof LocalDate) return Date.from(((LocalDate) date).atStartOfDay(ZoneId.of(Constant.LOC_SEOUL))
                .toInstant());
        if (date instanceof LocalDateTime) return Parser.localDateTimeToDate((LocalDateTime) date);
        return null;
    }

    /**
     * 날짜 또는 문자열을 받아서 LocalDate로 일괄 반환
     * (Date, 문자열, LocalDateTime)
     */
    public static LocalDate asLocalDate(final Object date) throws Exception {
        return LocalDate.from(asLocalDateTime(date));
    }

    /**
     * 날짜 또는 문자열을 받아서 LocalDateTime으로 일괄 반환
     * (Date, 문자열, LocalDateTime)
     */
    public static LocalDateTime asLocalDateTime(final Object date) throws Exception {
        final Date asDate = asDate(date);
        return LocalDateTime.ofInstant(asDate.toInstant(), ZoneId.systemDefault());
    }

    /**
     * 날짜 또는 문자열을 받아서 문자열String로 일괄 반환
     * @param date (Date, LocalDateTime, 문자열)
     * @param ptn (DatePtn enum)
     */
    public static String asStr(final Object date, final DatePtn ptn) throws Exception {
        if (date == null) return null;
        final Date asDate = asDate(date);
        return Parser.dateToStr(asDate, ptn);
    }

    /* ----- */

    /**
     * 현재 날짜Date 반환
     */
    public static Date getCurrDate() {
        final Calendar today = Calendar.getInstance(Constant.TZ_SEOUL, Constant.LC_KO);
        return new Date(today.getTimeInMillis());
    }

    /**
     * 현재 날짜LocalDate 반환
     */
    public static LocalDate getCurrLocalDate() throws Exception {
        return asLocalDate(getCurrDate());
    }

    /**
     * 현재 날짜LocalDateTime 반환
     */
    public static LocalDateTime getCurrLocalDateTime() throws Exception {
        return asLocalDateTime(getCurrDate());
    }

    /**
     * 현재 날짜 문자열String 반환
     * @param ptn (DatePtn enum)
     */
    public static String getCurrDateStr(final DatePtn ptn) throws Exception {
        return ptn.df.format(getCurrDate());
    }

    /**
     * 현재 날짜+시간 문자열String 반환
     */
    public static String getCurrDtStr() throws Exception {
        return DatePtn.DATETIME.df.format(getCurrDate());
    }

    /**
     * 현재 년도"yyyy"(int) 반환
     */
    public static Integer getCurrYy() throws Exception {
        return Calendar.getInstance(Constant.TZ_SEOUL, Constant.LC_KO)
                       .get(Calendar.YEAR);
    }

    /**
     * 현재 년도"yyyy" 문자열로 반환
     */
    public static String getCurrYyStr() throws Exception {
        return Integer.toString(getCurrYy());
    }

    /**
     * 현재 월"MM" 인덱스 반환 (1월=0)
     */
    public static Integer getCurrMnthIdx() throws Exception {
        return Calendar.getInstance(Constant.TZ_SEOUL, Constant.LC_KO)
                       .get(Calendar.MONTH);
    }

    public static Integer getCurrMnth() throws Exception {
        return Calendar.getInstance(Constant.TZ_SEOUL, Constant.LC_KO)
                       .get(Calendar.MONTH) + 1;
    }

    /**
     * 이전 월"MM" 인덱스 반환 (1월=0)
     */
    public static Integer getPrevMnthIdx() throws Exception {
        final int currMnthIdx = getCurrMnthIdx();
        return currMnthIdx == 0 ? 11 : currMnthIdx - 1;
    }

    public static Integer getPrevMnth() throws Exception {
        return getPrevMnthIdx() + 1;
    }

    /**
     * 다음 월"MM" 인덱스 반환 (1월=0)
     */
    public static Integer getNextMnthIdx() throws Exception {
        final int currMnthIdx = getCurrMnthIdx();
        return currMnthIdx == 11 ? 0 : currMnthIdx + 1;
    }

    public static Integer getNextMnth() throws Exception {
        return getNextMnthIdx() + 1;
    }

    /**
     * 현재 년도"yyyy"/월"MM" 세트 반환 (인덱스 대신 실제 월로 반환 = 1월=0 -> 1로 변환)
     */
    public static Integer[] getCurrYyMnth() throws Exception {
        return new Integer[]{getCurrYy(), Calendar.getInstance(Constant.TZ_SEOUL, Constant.LC_KO)
                                                    .get(Calendar.MONTH) + 1};
    }

    /**
     * 현재 년월"yyyyMM" 문자열
     */
    public static String getCurrYyMnthStr() throws Exception {
        final Integer[] currYyMnth = getCurrYyMnth();
        return currYyMnth[0] + String.format("%02d", currYyMnth[1]);
    }

    /**
     * 이전달의 년도"yyyy"/월"MM" 세트 반환 (인덱스 대신 실제 월로 반환 = 1월=1)
     */
    public static Integer[] getPrevYyMnth() throws Exception {
        final int currYy = getCurrYyMnth()[0];
        final int currMnth = getCurrYyMnth()[1];
        final int yy = (currMnth == 1) ? currYy - 1 : currYy;
        final int prevMnth = currMnth == 1 ? 12 : currMnth - 1;
        return new Integer[]{yy, prevMnth};
    }

    /**
     * 다음달의 년도"yyyy"/월"MM" 세트 반환 (인덱스 대신 실제 월로 반환 = 1월=1)
     */
    public static Integer[] getNextYyMnth() throws Exception {
        final int currYy = getCurrYyMnth()[0];
        final int currMnth = getCurrYyMnth()[1];
        final int yy = (currMnth == 12) ? currYy + 1 : currYy;
        final int nextMnth = currMnth == 12 ? 1 : currMnth + 1;
        return new Integer[]{yy, nextMnth};
    }

    /**
     * 어제 날짜Date 반환
     */
    public static Date getPrevDate() throws Exception {
        return getCurrDateAddDay(-1);
    }

    /**
     * 어제 날짜 문자열String 반환
     */
    public static String getPrevDateStr(final DatePtn ptn) throws Exception {
        return ptn.df.format(getPrevDate());
    }

    /**
     * 내일 날짜Date 반환
     */
    public static Date getNextDate() throws Exception {
        return getCurrDateAddDay(1);
    }

    /**
     * 내일 날짜 문자열String 반환
     */
    public static String getNextDateStr(final DatePtn ptn) throws Exception {
        return ptn.df.format(getNextDate());
    }

    /**
     * 현재 날짜Date에 기간(일자) 더해서 날짜Date로 반환
     */
    public static Date getCurrDateAddDay(final int dayCnt) throws Exception {
        final Calendar today = new GregorianCalendar(Constant.TZ_SEOUL, Constant.LC_KO);
        return getDateAddDay(today.getTime(), dayCnt);
    }

    /**
     * 현재 날짜Date에 기간(일자) 더해서 문자열String로 반환
     */
    public static String getCurrDateAddDayStr(final int dayCnt) throws Exception {
        return getCurrDateAddDayStr(dayCnt, DatePtn.DATETIME);
    }

    public static String getCurrDateAddDayStr(final int dayCnt, final DatePtn ptn) throws Exception {
        return Parser.dateToStr(getCurrDateAddDay(dayCnt), ptn);
    }

    /**
     * 날짜Date에 기간(일자) 더해서 날짜Date로 반환
     */
    public static Date getDateAddDay(final Object date, final int dayCnt) throws Exception {
        final Date asDate = asDate(date);
        if (asDate == null) return null;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(asDate);
        cal.add(Calendar.DATE, dayCnt);        // 일자 더하기
        return cal.getTime();
    }

    /**
     * 날짜Date에 기간(일자) 더해서 문자열String로 반환
     */
    public static String getDateAddDayStr(final Object date, final DatePtn ptn, final int dayCnt) throws Exception {
        return Parser.dateToStr(getDateAddDay(date, dayCnt), ptn);
    }

    /**
     * 날짜Date에 기간(년도) 더해서 날짜Date로 반환
     */
    public static Date getDateAddYy(final Object date, final int yyCnt) throws Exception {
        final Date asDate = asDate(date);
        if (asDate == null) return null;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(asDate);
        cal.add(Calendar.YEAR, yyCnt);        // 일자 더하기
        return cal.getTime();
    }

    /**
     * 날짜Date에 기간(년도) 더해서 문자열로 반환
     */
    public static String getDateAddYyStr(final Object date, final DatePtn ptn, final int yyCnt) throws Exception {
        return Parser.dateToStr(getDateAddYy(date, yyCnt), ptn);
    }

    /**
     * 날짜Date에 기간(분) 더해서 날짜Date로 반환
     */
    public static Date addMinutes(final Object date, final int minCnt) throws Exception {
        final Date asDate = asDate(date);
        if (asDate == null) return null;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(asDate);
        cal.add(Calendar.MINUTE, minCnt);        // 일자 더하기
        return cal.getTime();
    }

    /**
     * 두 날짜 사이의 차이 반환
     */
    public static Long getDateDiff(final Object fromDate, final Object toDate, final TimeUnit timeUnit) throws Exception {
        final Date asFromDate = asDate(fromDate);
        final Date asToDate = asDate(toDate);
        if (asFromDate == null || asToDate == null) return null;
        long diffInMillies = asToDate.getTime() - asFromDate.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    /**
     * 날짜 받아서 요일(문자) 반환
     */
    public static String getDayOfWeekChinese(final Object date) throws Exception {
        final Integer idx = getDayOfWeekIdx(date);
        return DayOfWeek.asChinese(idx);
    }

    /**
     * 날짜 받아서 요일(문자) 반환
     */
    public static String getDayOfWeekKor(final Object date) throws Exception {
        final Integer idx = getDayOfWeekIdx(date);
        return DayOfWeek.asKorean(idx);
    }

    /**
     * 날짜 받아서 요일(숫자) 반환
     * "1은 일요일, 7은 토요일을 나타냅니다."
     */
    public static Integer getDayOfWeekIdx(final Object date) throws Exception {
        final Date asDate = asDate(date);
        if (asDate == null) return null;
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(asDate);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * 해당 주의 월요일 구하기
     */
    public static Date getFirstDayOfWeek(final Object date) throws Exception {
        final Date asDate = asDate(date);
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(asDate);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return calendar.getTime();
    }

    public static Date getFirstDayOfCurrWeek() throws Exception {
        final Date currDate = getCurrDate();
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(currDate);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return calendar.getTime();
    }

    /** 문자열이 날짜형식인지 체크 */
    public static Boolean isDateStr(final String dateStr) {
        try {
            DateUtils.asDate(dateStr);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** 날짜 받아서 주말여부 반환 */
    public static Boolean isWeekend(final Object date) throws Exception {
        return Arrays.asList(1, 7)
                .contains(DateUtils.getDayOfWeekIdx(date));
    }

    /** 두 날짜를 받아서 같은날짜 여부 반환 */
    public static boolean isSameDay(
            final Object date1,
            final Object date2
    ) throws Exception {
        final String date1str = DateUtils.asStr(date1, DatePtn.PDATE);
        final String date2str = DateUtils.asStr(date2, DatePtn.PDATE);
        return date1str.equals(date2str);
    }

}