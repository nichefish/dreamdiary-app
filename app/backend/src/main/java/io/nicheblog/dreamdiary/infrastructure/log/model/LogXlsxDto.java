package io.nicheblog.dreamdiary.infrastructure.log.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 로그 목록 엑셀 다운로드 Dto.
 */
@Getter
@Setter
@NoArgsConstructor
public class LogXlsxDto {

    /** 작업일시 */
    private String logDt;

    /** 작업자 계정명 */
    private String username;

    /** 작업자 이름 */
    private String userNm;

    /** 작업자 IP */
    private String ipAddr;

    /** 작업 URL */
    private String url;

    /** 작업 결과 */
    private String rslt;
}
