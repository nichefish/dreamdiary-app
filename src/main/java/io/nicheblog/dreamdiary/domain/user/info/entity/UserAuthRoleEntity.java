package io.nicheblog.dreamdiary.domain.user.info.entity;

import io.nicheblog.dreamdiary.auth.entity.AuthRoleEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;

/**
 * UserAuthRoleEntity
 * <pre>
 *  사용자-권한 Entity
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "user_auth_role")
@DynamicInsert      // null인 값은 (null로 insert하는 대신) insert에서 제외
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE user_auth_role SET del_yn = 'Y' WHERE user_auth_role_no = ?")
public class UserAuthRoleEntity
        extends BaseCrudEntity {

    @PostLoad
    private void onLoad() {
        // 코드 이름 세팅
        if (this.roleInfo != null) this.authNm = this.roleInfo.getAuthNm();
    }

    /** 사용자-권한 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_auth_role_no")
    @Comment("사용자 권한 번호 (PK)")
    private Integer userAuthRoleNo;

    /** 사용자 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_no", referencedColumnName = "user_no", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("사용자 정보")
    private UserEntity user;

    /** 권한 코드 */
    @Column(name = "auth_cd")
    @Comment("권한 코드")
    private String authCd;

    /** 권한 정보 매핑 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "auth_cd", referencedColumnName = "auth_cd", insertable = false, updatable = false)
    @Fetch(value = FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("작업자 정보")
    private AuthRoleEntity roleInfo;

    /** 권한 이름 */
    @Transient
    private String authNm;
    
    /* ----- */

    /**
     * 생성자.
     * @param authCd - 사용자의 권한 코드를 나타내는 문자열
     */
    public UserAuthRoleEntity(final String authCd) {
        this.authCd = authCd;
    }
}
