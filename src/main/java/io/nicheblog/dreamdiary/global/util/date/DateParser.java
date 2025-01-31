package io.nicheblog.dreamdiary.global.util.date;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

/**
 * DateParser
 * <pre>
 *  날짜 정보 관련 처리 유틸리티 모듈
 *  (!package-private class)
 * </pre>
 *
 * @author nichefish
 */
public class DateParser {

    /**
     * 해당날짜의 시작시간 반환 (ex: 2021-09-15 00:00:00)
     *
     * @param paramDate 변환할 날짜 (`String`, `Date`, `LocalDate`, `LocalDateTime` 등 지원)
     * @return {@link Date} 변환된 날짜의 시작 시간 (`Date` 객체, 예: `2021-09-15 00:00:00`), 입력값이 `null`이면 `null` 반환
     * @throws Exception 날짜 변환 중 오류가 발생할 경우
     */
    public static Date sDateParse(final Object paramDate) throws Exception {
        final Date searchDate = DateUtils.asDate(paramDate);
        if (searchDate == null) return null;
        final LocalDateTime startOfDay = DateUtils.asLocalDateTime(searchDate).with(LocalTime.MIN);
        return localDateTimeToDate(startOfDay);
    }

    /**
     * 해당날짜의 시작시간 문자열 반환 (ex: "2021-09-15 00:00:00")
     */
    public static String sDateParseStr(final Object paramDate, final DatePtn ptn) throws Exception {
        final Date sDate = sDateParse(paramDate);
        return dateToStr(sDate, ptn);
    }

    /**
     * 해당날짜의 시작시간 문자열 반환 (ex: "2021-09-15 00:00:00")
     */
    public static String sDateParseStr(final Object paramDate) throws Exception {
        final Date sDate = sDateParse(paramDate);
        return dateToStr(sDate, DatePtn.DATE);
    }

    /**
     * 해당날짜의 끝 시간 반환 (ex: 2021-09-15 23:59:59)
     */
    public static Date eDateParse(final Object paramDate) throws Exception {
        final Date searchDate = DateUtils.asDate(paramDate);
        if (searchDate == null) return null;

        final LocalDateTime endOfDay = DateUtils.asLocalDateTime(searchDate).with(LocalTime.MAX);
        return localDateTimeToDate(endOfDay);
    }

    /**
     * 해당날짜의 끝 시간 문자열 반환 (ex: "2021-09-15 23:59:59")
     */
    public static String eDateParseStr(final Object paramDate) throws Exception {
        final Date eDate = eDateParse(paramDate);
        return dateToStr(eDate, DatePtn.DATETIME);
    }
    public static String eDateParseStr(final Object paramDate, final DatePtn ptn) throws Exception {
        final Date eDate = eDateParse(paramDate);
        return dateToStr(eDate, ptn);
    }
    /**
     * 해당날짜의 24시간 전 반환
     */
    public static Date bfDateParse(final Object paramDate) throws Exception {
        Date searchDate = DateUtils.asDate(paramDate);
        if (searchDate == null) return null;

        searchDate = DateUtils.getDateAddDay(searchDate, -1);
        if (searchDate == null) return null;

        final LocalDateTime endOfDay = DateUtils.asLocalDateTime(searchDate).with(LocalTime.MAX);
        return localDateTimeToDate(endOfDay);
    }

    /**
     * 날짜Date를 문자열String로 변환
     */
    public static String dateToStr(final Date date, final DatePtn ptn) {
        if (date == null) return "";
        return ptn.df.format(date);
    }

    /**
     * 문자열String을 날짜Date로 변환
     * 패턴을 별도로 받지 않고 정의된 패턴을 전부 체크한다.
     */
    public static Date strToDate(final String dateStrParam) throws Exception {
        if (StringUtils.isEmpty(dateStrParam)) return null;

        final String[] parsePatterns = Arrays.stream(DatePtn.values())
                .map(datePtn -> datePtn.pattern)
                .toArray(String[]::new);
        // microsecond 포함/미포함이 섞여서 넘어오는 문제 해결 위해 microsecond 미사용 처리
        final String dateStr = (dateStrParam.length() > 20) ? dateStrParam.substring(0, 19) : dateStrParam;
        return DateUtils.parseDateStrictly(dateStr, parsePatterns);
    }

    /**
     * 문자열String을 날짜Date로 변환
     * 패턴을 받아서 특정 패턴으로만 해석한다.
     */
    public static Date strToDate(final String dateStrParam, final DatePtn ptn) throws Exception {
        if (StringUtils.isEmpty(dateStrParam)) return null;

        // microsecond 포함/미포함이 섞여서 넘어오는 문제 해결 위해 microsecond 미사용 처리
        final String dateStr = (dateStrParam.length() > 20) ? dateStrParam.substring(0, 19) : dateStrParam;
        return ptn.df.parse(dateStr);
    }

    /**
     * LocalDateTime을 Date로 변환
     */
    public static Date localDateTimeToDate(final LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

}
