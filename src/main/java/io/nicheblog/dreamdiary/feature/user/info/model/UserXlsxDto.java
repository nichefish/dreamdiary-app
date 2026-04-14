package io.nicheblog.dreamdiary.feature.user.info.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * UserXlsxDto
 * <pre>
 *  사용자(계정) 정보 엑셀 다운로드 Dto
 *  (필드 정의 후 XlsxUtils에서 속성을 reflection으로 읽어 동적으로 처리)
 *  (헤더:: XlsxConstant에 정의)
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
public class UserXlsxDto {

    /** 권한 */
    private String authNm;

    /** 계정명  */
    private String username;

    /** 이름 */
    private String nickNm;

    /** 연락처(전화번호) */
    private String cttpc;

    /** E-mail */
    private String email;

    /** 잠금 여부 (Y/N) */
    private String lockedYn;

    /** 접속 IP 정보 (String) */
    private String acsIpListStr;

    /** 등록자 ID */
    protected String regstrId;

    /** 최종접속일시 */
    private String lstLgnDt;

}
