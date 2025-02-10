package io.nicheblog.dreamdiary.extension.log.actvty.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * LogActvtyListXlsxDto
 * <pre>
 *  활동 로그 목록 엑셀 다운로드 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
public class LogActvtyXlsxDto {

    /** 작업일시 */
    private String logDt;

    /** 작업자 ID */
    private String userId;

    /** 작업자 이름 */
    private String userNm;

    /** 작업자 IP */
    private String ipAddr;

    /** 작업 URL */
    private String url;

    /** 작업 결과 */
    private String rslt;
}
