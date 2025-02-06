package io.nicheblog.dreamdiary.domain.user.info.model;

import io.nicheblog.dreamdiary.auth.security.entity.AuthRoleEntity;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * UserAuthRoleDto
 * <pre>
 *  사용자-권한 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserAuthRoleDto
        extends BaseCrudDto {

    /** 사용자 권한 번호 (PK) */
    private Integer userAuthRoleNo;

    /** 권한 코드 */
    private String authCd;

    /** 권한 코드 */
    private String authNm;

    /** 권한 정보 매핑 */
    private AuthRoleEntity role;
}
