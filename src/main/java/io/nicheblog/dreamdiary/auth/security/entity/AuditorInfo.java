package io.nicheblog.dreamdiary.auth.security.entity;

import io.nicheblog.dreamdiary.feature.user.info.entity.UserAuthRoleEntity;
import io.nicheblog.dreamdiary.global.Constant;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;

/**
 * AuditorInfo
 * <pre>
 *  (공통) Auditor(createdBy, updatedBy) 정보 Entity.
 *  연관관계 조회시에만 사용. 상호참조로 인한 무한재귀호출 방지를 위해서 UserEntity와 분리
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AuditorInfo
        implements Serializable {

    /** 사용자 ID */
    @Id
    @Column(name = "id", length = 20, insertable = false, updatable = false)
    private Integer id;

    /** 사용자 ID */
    @Column(name = "username", length = 20, insertable = false, updatable = false)
    private String username;

    /** 사용자 이름 */
    @Column(name = "nickname", length = 20, insertable = false, updatable = false)
    private String nickname;

    /** 사용자 권한 정보 */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    @Fetch(FetchMode.SELECT)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("사용자 권한 정보")
    private List<UserAuthRoleEntity> authList;

    /** 프로필 이미지 URL */
    @Column(name = "profile_image_url", length = 1000)
    private String profileImageUrl;

    /* ----- */

    /**
     * 프로필 이미지 getter 재정의
     */
    public String getProfileImageUrl() {
        if (StringUtils.isEmpty(this.profileImageUrl)) return (Constant.BLANK_AVATAR_URL);

        return this.profileImageUrl;
    }
}
