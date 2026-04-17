package io.nicheblog.dreamdiary.feature.user.info.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * UserPhoneNumberXlsxDto
 * <pre>
 *  사용자 연락처 엑셀 다운로드 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class UserPhoneNumberXlsxDto {

    /** 이름 */
    private String userNm;

    /** 직급이름 */
    private String rankNm;

    /** 소속(팀) */
    private String teamNm;

    /** 연락처(전화번호) */
    private String phoneNumber;

    /** E-mail */
    private String email;
}
