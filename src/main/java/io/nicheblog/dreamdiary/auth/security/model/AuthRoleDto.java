package io.nicheblog.dreamdiary.auth.security.model;

import io.nicheblog.dreamdiary.extension.clsf.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * AuthRoleDto
 * <pre>
 *  (공통) 권한 정보 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class AuthRoleDto
        extends BaseCrudDto
        implements Identifiable<String> {

    /** 권한 코드 (PK) */
    private String authCd;
    /** 권한 이름 */
    private String authNm;
    /** 권한 레벨 */
    private Integer authLevel;
    /** 상위 권한 코드 (null일시 최상위 권한) */
    private String topAuthCd;
    /** 하위 권한 정보 */
    private List<AuthRoleDto> subAuthList;

    /* ----- */

    @Override
    public String getKey() {
        return this.authCd;
    }

    /** 위임 :: 상태 관리 모듈 */
    public StateCmpstn state;
}