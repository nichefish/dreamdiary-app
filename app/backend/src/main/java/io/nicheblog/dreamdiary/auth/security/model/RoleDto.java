package io.nicheblog.dreamdiary.auth.security.model;

import io.nicheblog.dreamdiary.feature.attachable.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.state.model.cmpstn.StateCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * RoleDto
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
public class RoleDto
        extends BaseCrudDto
        implements Identifiable<Integer>, StateCmpstnModule {

    /** 내부 PK (id) */
    private Integer id;
    /** 역할 키 (비즈니스 키) */
    private String roleKey;
    /** 역할 표시명 */
    private String roleName;
    /** 권한 레벨 */
    private Integer authLevel;
    /** 상위 권한 ID (null이면 최상위) */
    private Integer parentRoleId;
    /** 정렬 순서 */
    private Integer sortOrder;
    /** 사용 여부 (목록 표시용; 엔티티 {@code useYn} 과 동일) */
    private String useYn;
    /** 하위 역할 목록 */
    private List<RoleDto> subRoleList;

    /* ----- */

    @Override
    public Integer getKey() {
        return this.id;
    }

    /** 위임 :: 상태 관리 모듈 */
    public StateCmpstn state;
}
