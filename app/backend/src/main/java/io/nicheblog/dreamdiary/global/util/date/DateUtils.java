package io.nicheblog.dreamdiary.global.util.date;

import io.nicheblog.dreamdiary.global.Constant;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * DateUtils
 * <pre>
 *  날짜 연산 관련 로직 처리 유틸리티 모듈.
 * </pre>
 *
 * 변경 전: commons.lang3.time.DateUtils 상속으로 apache 함수를 겸용 + 공유 SimpleDateFormat(DF_DATE) 보유.
 * 변경 후: 상속 제거 — 실제 사용되던 lang3 상속 메서드(isSameDay, addDays)는 동등 시그니처로 로컬 정의,
 * parseDateStrictly 는 DateParser 에서 lang3 직접 호출. 미사용 공유 포맷터(DF_DATE)는 제거 (스레드 세이프 아님).
 *
 * Phase 3(유틸 축소): 호출처 0 인 dead API 19종 제거 — 신규 코드는 java.time 을 직접 사용하고,
 * 이 유틸은 문자열↔날짜 변환(asDate/asStr/asLocalDate(Time))과 레거시 호환 연산만 유지한다.
 *
 * @author nichefish
 */
@UtilityClass
public class DateUtils {

    /** 날짜 파싱 관련 메소드 위임 */
    public static class Parser extends DateParser {}
    public Parser parser = new Parser();

    /** 음력 관련 메소드 위임 */
    public static class ChineseCal extends ChineseCalModule {}

    public static final String PTN_DATE = "yyyy-MM-dd";
    public static final String PTN_DATETIME = "yyyy-MM-dd HH:mm:ss";

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
     * (변경 전: null 입력 시 asDate(null)=null 경유 NPE. 변경 후: null 반환 - asDate/asStr 와 동일 계약)
     */
    public static LocalDate asLocalDate(final Object date) throws Exception {
        final LocalDateTime localDateTime = asLocalDateTime(date);
        return localDateTime == null ? null : LocalDate.from(localDateTime);
    }

    /**
     * 날짜 또는 문자열을 받아서 LocalDateTime으로 일괄 반환
     * (Date, 문자열, LocalDateTime)
     * (변경 전: null 입력 시 asDate(null)=null 경유 NPE. 변경 후: null 반환 - asDate/asStr 와 동일 계약)
     */
    public static LocalDateTime asLocalDateTime(final Object date) throws Exception {
        final Date asDate = asDate(date);
        if (asDate == null) return null;
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
     * 현재 날짜LocalDateTime 반환
     * (변경 전: asLocalDateTime 경유로 선언적 throws Exception 이 바이럴 전파. 변경 후: 예외 경로가 없어 throws 제거 — lambda 등에서 직접 사용 가능)
     */
    public static LocalDateTime getCurrLocalDateTime() {
        return LocalDateTime.ofInstant(getCurrDate().toInstant(), ZoneId.systemDefault());
    }

    /**
     * 현재 날짜 문자열String 반환
     * @param ptn (DatePtn enum)
     */
    public static String getCurrDateStr(final DatePtn ptn) {
        return ptn.format(getCurrDate());
    }

    /**
     * 현재 년도"yyyy"(int) 반환
     */
    public static Integer getCurrYy() {
        return Calendar.getInstance(Constant.TZ_SEOUL, Constant.LC_KO)
                       .get(Calendar.YEAR);
    }

    /**
     * 현재 년도"yyyy" 문자열로 반환
     */
    public static String getCurrYyStr() {
        return Integer.toString(getCurrYy());
    }

    /**
     * 현재 월"MM" 인덱스 반환 (1월=0)
     */
    public static Integer getCurrMnthIdx() {
        return Calendar.getInstance(Constant.TZ_SEOUL, Constant.LC_KO)
                       .get(Calendar.MONTH);
    }

    public static Integer getCurrMnth() {
        return Calendar.getInstance(Constant.TZ_SEOUL, Constant.LC_KO)
                       .get(Calendar.MONTH) + 1;
    }

    /**
     * 현재 년도"yyyy"/월"MM" 세트 반환 (인덱스 대신 실제 월로 반환 = 1월=0 -> 1로 변환)
     */
    public static Integer[] getCurrYyMnth() {
        return new Integer[]{getCurrYy(), Calendar.getInstance(Constant.TZ_SEOUL, Constant.LC_KO)
                                                    .get(Calendar.MONTH) + 1};
    }

    /**
     * 현재 년월"yyyyMM" 문자열
     */
    public static String getCurrYyMnthStr() {
        final Integer[] currYyMnth = getCurrYyMnth();
        return currYyMnth[0] + String.format("%02d", currYyMnth[1]);
    }

    /**
     * 이전달의 년도"yyyy"/월"MM" 세트 반환 (인덱스 대신 실제 월로 반환 = 1월=1)
     */
    public static Integer[] getPrevYyMnth() {
        final int currYy = getCurrYyMnth()[0];
        final int currMnth = getCurrYyMnth()[1];
        final int yy = (currMnth == 1) ? currYy - 1 : currYy;
        final int prevMnth = currMnth == 1 ? 12 : currMnth - 1;
        return new Integer[]{yy, prevMnth};
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
        return ptn.format(getNextDate());
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
     * 날짜 받아서 요일(문자) 반환
     */
    public static String getDayOfWeekChinese(final Object date) throws Exception {
        final Integer idx = getDayOfWeekIdx(date);
        return DayOfWeek.asChinese(idx);
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
     * 해당 날짜가 포함된 주의 시작일(월요일) 반환
     */
    public static Date getWeekStartDate(final Object date) throws Exception {
        final LocalDate localDate = asLocalDate(date);

        final LocalDate weekStart = localDate.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        return asDate(weekStart);
    }

    /**
     * 해당 날짜가 포함된 주의 시작일(월요일) 문자열 반환
     */
    public static String getWeekStartDateStr(final Object date) throws Exception {
        return asStr(getWeekStartDate(date), DatePtn.DATE);
    }

    /** 날짜 받아서 주말여부 반환 */
    public static Boolean isWeekend(final Object date) throws Exception {
        return Arrays.asList(1, 7).contains(DateUtils.getDayOfWeekIdx(date));
    }

    /** 두 날짜를 받아서 같은날짜 여부 반환 */
    public static boolean isSameDay(final Object date1, final Object date2) throws Exception {
        final String date1str = DateUtils.asStr(date1, DatePtn.PDATE);
        final String date2str = DateUtils.asStr(date2, DatePtn.PDATE);
        return date1str.equals(date2str);
    }

    /**
     * 두 날짜Date를 받아서 같은날짜 여부 반환.
     * (변경 전: lang3 상속 static 사용. 변경 후: 상속 제거로 동등 시그니처 로컬 정의 —
     * checked exception 없음: Mapstruct 표현식 등 throws 불가 맥락에서 사용. null 시 IllegalArgumentException, lang3 동일)
     */
    public static boolean isSameDay(final Date date1, final Date date2) {
        if (date1 == null || date2 == null) throw new IllegalArgumentException("The date must not be null");
        final Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        final Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
                && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * 두 LocalDateTime 을 받아서 같은날짜 여부 반환.
     * (Phase 2-B: 엔티티 LocalDateTime 전환에 따른 unchecked 오버로드 — Mapstruct 표현식 등 throws 불가 문맥용. null 시 IllegalArgumentException, Date 버전과 동일 계약)
     */
    public static boolean isSameDay(final LocalDateTime date1, final LocalDateTime date2) {
        if (date1 == null || date2 == null) throw new IllegalArgumentException("The date must not be null");
        return date1.toLocalDate().isEqual(date2.toLocalDate());
    }

    /**
     * 날짜Date에 일자를 더해서 반환.
     * (변경 전: lang3 상속 static 사용. 변경 후: 상속 제거로 동등 시그니처 로컬 정의 — null 시 NullPointerException, lang3 동일)
     */
    public static Date addDays(final Date date, final int amount) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, amount);
        return cal.getTime();
    }

}
