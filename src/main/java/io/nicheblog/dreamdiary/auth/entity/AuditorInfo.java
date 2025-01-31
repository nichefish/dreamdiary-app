package io.nicheblog.dreamdiary.auth.entity;

import io.nicheblog.dreamdiary.domain.user.info.entity.UserAuthRoleEntity;
import io.nicheblog.dreamdiary.global.Constant;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * AuditorInfo
 * <pre>
 *  (공통) Auditor(regstr, mdfusr) 정보 Entity.
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
@Where(clause = "del_yn='N'")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AuditorInfo
        implements Serializable {

    /** 사용자 번호 (PK) */
    @Id
    @Column(name = "user_no", length = 20, insertable = false, updatable = false)
    private Integer userNo;

    /** 사용자 ID */
    @Column(name = "user_id", length = 20, insertable = false, updatable = false)
    private String userId;

    /** 사용자 이름 */
    @Column(name = "nick_nm", length = 20, insertable = false, updatable = false)
    private String nickNm;

    /** 사용자 권한 정보 */
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_no")
    @Fetch(FetchMode.SELECT)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("사용자 권한 정보")
    private List<UserAuthRoleEntity> authList;

    /** 프로필 이미지 URL */
    @Column(name = "profl_img_url", length = 1000)
    private String proflImgUrl;

    /* ----- */

    /**
     * 프로필 이미지 getter 재정의
     */
    public String getProflImgUrl() {
        if (StringUtils.isEmpty(this.proflImgUrl)) return (Constant.BLANK_AVATAR_URL);

        return this.proflImgUrl;
    }
}