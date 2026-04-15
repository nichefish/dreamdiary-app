package io.nicheblog.dreamdiary.feature.admin.log.sys.model;

import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * LogSysQueryDto
 * <pre>
 *  시스템 로그 조회 전용 DTO.
 * </pre>
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LogSysQueryDto
        extends BaseCrudDto
        implements Identifiable<Integer> {

    /** 로그 고유 번호 */
    private Integer id;

    /** 작업자 계정명 */
    private String username;

    /** 작업자 이름 */
    private String userNm;

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

    /** 작업 내용 */
    private String content;

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

    @Override
    public Integer getKey() {
        return this.id;
    }

}
