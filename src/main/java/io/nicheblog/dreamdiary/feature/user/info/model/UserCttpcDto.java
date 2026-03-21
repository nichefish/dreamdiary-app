package io.nicheblog.dreamdiary.feature.user.info.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * UserCttpcDto
 * <pre>
 *  사용자 연락처 조회 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class UserCttpcDto {

    /** 이름 */
    private String userNm;

    /** 직급이름 */
    private String rankNm;

    /** 소속(팀 부서) */
    private String teamNm;

    /** 연락처(전화번호) */
    private String cttpc;

    /** E-mail */
    private String email;
}
