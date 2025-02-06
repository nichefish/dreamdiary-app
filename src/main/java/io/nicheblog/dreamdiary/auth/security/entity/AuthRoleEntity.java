package io.nicheblog.dreamdiary.auth.security.entity;

import io.nicheblog.dreamdiary.extension.state.entity.embed.StateEmbed;
import io.nicheblog.dreamdiary.extension.state.entity.embed.StateEmbedModule;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.List;

/**
 * AuthRoleEntity
 * <pre>
 *  (공통) 권한 정보 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "auth_role")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
public class AuthRoleEntity
        extends BaseCrudEntity
        implements StateEmbedModule {

    /** 권한 코드 (PK) */
    @Id
    @Column(name = "auth_cd", length = 50)
    private String authCd;

    /** 권한 이름 */
    @Column(name = "auth_nm", length = 50)
    private String authNm;

    /** 권한 레벨 */
    @Column(name = "auth_level")
    private Integer authLevel;

    /** 상위 권한 코드 (null일시 최상위 권한) */
    @Column(name = "top_auth_cd", length = 50)
    private String topAuthCd;

    /** 하위 권한 정보 */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "auth_cd", referencedColumnName = "top_auth_cd", insertable = false, updatable = false)
    @Fetch(value = FetchMode.JOIN)
    @OrderBy("state.sortOrdr ASC")
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("하위 권한 정보")
    private List<AuthRoleEntity> subAuthList;

    /* ----- */

    /** 위임 :: 상태 관리 모듈 */
    @Embedded
    public StateEmbed state;
}