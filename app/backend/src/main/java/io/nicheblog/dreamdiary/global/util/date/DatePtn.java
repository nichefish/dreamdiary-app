package io.nicheblog.dreamdiary.global.util.date;

import lombok.RequiredArgsConstructor;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * DatePtn
 * <pre>
 *  날짜 포맷 패턴 정의 enum.
 * </pre>
 *
 * 변경 전: enum 상수 필드로 {@link SimpleDateFormat} 인스턴스를 공유 — SimpleDateFormat 은 스레드 세이프하지 않아
 * 동시 요청 시 parse/format 결과가 오염될 수 있었다.
 * 변경 후: 공유 인스턴스를 제거하고 {@link #format(Date)}/{@link #parse(String)} 호출마다 새 인스턴스를 생성한다.
 * (파싱 관용(lenient)·밀리초 'S' 표기 등 SimpleDateFormat 의 기존 해석 계약은 그대로 유지 — java.time 전환은 후속 phase)
 *
 * @author nichefish
 */
@RequiredArgsConstructor
public enum DatePtn {

    DATE("yyyy-MM-dd"),
    DATEDY("yyyy.MM.dd '('EEE')'"),
    DATETIME("yyyy-MM-dd HH:mm:ss"),
    LDATETIME("yyyyy-MM-dd HH:mm:ss"),      // 머신러닝측 날짜포맷 실수?커버위해 작성
    PDATE("yyyyMMdd"),
    PDATETIME("yyyyMMddHHmmss"),
    MDATETIME("yyyyMMddHHmm"),
    ZDATETIME("yyyy-MM-dd'T'HH:mm:ss"),
    DATETIMES("yyyy-MM-dd HH:mm:ss.S"),
    SDATE("yyyy/MM/dd"),
    SDATETIME("yyyy/MM/dd HH:mm:ss"),
    KR_DATETIME("yy. M. d. a HH:mm"),
    BRTHDY("MM월 dd일"),
    TIME("HH:mm:ss");

    public final String pattern;

    /**
     * 호출마다 새 {@link SimpleDateFormat} 을 생성한다. (스레드 간 공유 금지 계약)
     *
     * @return {@link SimpleDateFormat} -- 이 패턴의 새 포맷터 인스턴스
     */
    private SimpleDateFormat newFormat() {
        if (this == KR_DATETIME) {
            // 한국어 오전/오후 표기 강제 (기존 공유 인스턴스의 DateFormatSymbols 커스터마이즈와 동일)
            final SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.KOREA);
            final DateFormatSymbols symbols = new DateFormatSymbols(Locale.KOREA);
            symbols.setAmPmStrings(new String[]{"오전", "오후"});
            sdf.setDateFormatSymbols(symbols);
            return sdf;
        }
        return new SimpleDateFormat(pattern);
    }

    /**
     * 날짜를 이 패턴의 문자열로 포맷한다.
     *
     * @param date 포맷할 날짜
     * @return {@link String} -- 포맷된 문자열
     */
    public String format(final Date date) {
        return newFormat().format(date);
    }

    /**
     * 문자열을 이 패턴으로 파싱한다. (기존 SimpleDateFormat 과 동일하게 lenient)
     *
     * @param dateStr 파싱할 문자열
     * @return {@link Date} -- 파싱된 날짜
     * @throws ParseException 패턴과 불일치 시
     */
    public Date parse(final String dateStr) throws ParseException {
        return newFormat().parse(dateStr);
    }
}
