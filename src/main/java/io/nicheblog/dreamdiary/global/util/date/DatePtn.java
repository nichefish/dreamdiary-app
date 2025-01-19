package io.nicheblog.dreamdiary.global.util.date;

import lombok.RequiredArgsConstructor;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * ContentType
 * <pre>
 *  ClsfEntity를 상속받은 클래스들이 사용하는 컨텐츠 타입 정보
 * </pre>
 *
 * @author nichefish
 */
@RequiredArgsConstructor
public enum DatePtn {

    DATE("yyyy-MM-dd", new SimpleDateFormat("yyyy-MM-dd")),
    DATEDY("yyyy.MM.dd '('EEE')'", new SimpleDateFormat("yyyy.MM.dd '('EEE')'")),
    DATETIME("yyyy-MM-dd HH:mm:ss", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")),
    LDATETIME("yyyyy-MM-dd HH:mm:ss", new SimpleDateFormat("yyyyy-MM-dd HH:mm:ss")),      // 머신러닝측 날짜포맷 실수?커버위해 작성
    PDATE("yyyyMMdd", new SimpleDateFormat("yyyyMMdd")),
    PDATETIME("yyyyMMddHHmmss", new SimpleDateFormat("yyyyMMddHHmmss")),
    MDATETIME("yyyyMMddHHmm", new SimpleDateFormat("yyyyMMddHHmm")),
    ZDATETIME("yyyy-MM-dd'T'HH:mm:ss", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")),
    DATETIMES("yyyy-MM-dd HH:mm:ss.S", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S")),
    SDATE("yyyy/MM/dd", new SimpleDateFormat("yyyy/MM/dd")),
    SDATETIME("yyyy/MM/dd HH:mm:ss", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")),
    KR_DATETIME("yy. M. d. a HH:mm", new SimpleDateFormat("yy. M. d. a HH:mm", Locale.KOREA) {
        {
            DateFormatSymbols symbols = new DateFormatSymbols(Locale.KOREA);
            symbols.setAmPmStrings(new String[]{"오전", "오후"});
            this.setDateFormatSymbols(symbols);
        }
    }),
    BRTHDY("MM월 dd일", new SimpleDateFormat("MM월 dd일")),
    TIME("HH:mm:ss", new SimpleDateFormat("HH:mm:ss"));

    public final String pattern;
    public final SimpleDateFormat df;
}