package io.nicheblog.dreamdiary.extension.log.actvty.model;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;

/**
 * LogActvtyDto
 * <pre>
 *  활동 로그 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class LogActvtyDto
        extends BaseCrudDto
        implements Identifiable<Integer> {

    /** 로그 고유 번호 (PK) */
    private Integer logActvtyNo;
    /** 작업자 ID */
    private String userId;
    /** 작업자 이름 */
    private String logUserNm;
    /** 권한코드 */
    private String authCd;
    /** 권한명 */
    private String authNm;
    /** 작업일시 */
    private String logDt;

    /** 작업 구분 코드 (ex. 게시판, 공지사항, ...) (기능/모듈 단위) */
    private String actvtyCtgrCd;
    /** 작업 구분 코드 (ex. 게시판, 공지사항, ...) (기능/모듈 단위) */
    private String actvtyCtgrNm;
    /** 작업 유형 코드 (조회, 검색, 제출, 처리...) */
    private String actionTyCd;
    /** 작업 유형 코드 (조회, 검색, 제출, 처리...) */
    private String actionTyNm;

    /** 작업 URL */
    private String url;
    /** 작업 URL 이름 */
    private String urlNm;
    /** 작업 파라미터 */
    private String param;
    /** 작업 파라미터 맵 */
    private HashMap<String, String> paramMap;

    /** 메소드 */
    private String mthd;
    /** 작업 내용 */
    private String cn;

    /** 작업자 IP */
    private String ipAddr;
    /** 리퍼러 */
    private String referer;

    /** 작업 결과 */
    private String rslt;
    /** 작업 결과 메세지 */
    private String rsltMsg;
    /** 익셉션 이름 */
    private String exceptionNm;
    /** 익셉션 메세지 */
    private String exceptionMsg;

    /* ----- */

    /**
     * Getter :: 성공여부
     *
     * @return {@link Boolean} -- 성공여부 반환
     */
    public Boolean isSuccess() {
        return "true".equals(this.rslt);
    }

    /**
     * Getter :: 작업자 여부
     */
    public Boolean getIsActvtyUser() {
        return (AuthUtils.isRegstr(this.userId));
    }

    /**
     * Getter :: 작업 파라미터 = "null"은 표시하지 않음
     */
    public String getParam() {
        if ("null".equals(this.param)) return "-";
        return this.param;
    }

    /**
     * 활동 로그 상세 (DTL) Dto.
     */
    @Getter
    @Setter
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class DTL
            extends LogActvtyDto {
        //
    }


    /**
     * 활동 로그 목록 조회 (LIST) Dto.
     */
    @Getter
    @Setter
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class LIST
            extends LogActvtyDto {
        //
    }

    /* ----- */

    @Override
    public Integer getKey() {
        return this.logActvtyNo;
    }
}
