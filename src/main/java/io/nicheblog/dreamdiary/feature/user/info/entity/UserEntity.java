package io.nicheblog.dreamdiary.feature.user.info.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.file.entity.embed.FileEmbed;
import io.nicheblog.dreamdiary.feature.file.entity.embed.FileEmbedModule;
import io.nicheblog.dreamdiary.feature.user.emplym.entity.UserEmplymEntity;
import io.nicheblog.dreamdiary.feature.user.emplym.mapstruct.UserEmplymMapstruct;
import io.nicheblog.dreamdiary.feature.user.info.model.emplym.UserEmplymDto;
import io.nicheblog.dreamdiary.feature.user.info.model.profile.UserProfileDto;
import io.nicheblog.dreamdiary.feature.user.profile.entity.UserProfileEntity;
import io.nicheblog.dreamdiary.feature.user.profile.mapstruct.UserProfileMapstruct;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UserEntity
 * <pre>
 *  계정 정보 Entity :: 사용자 정보(UserInfo)를 위임 필드로 가짐
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "user")
@DynamicInsert      // null인 값은 (null로 insert하는 대신) insert에서 제외
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE user SET deleted_at = NOW() WHERE id = ?")
public class UserEntity
        extends BaseAttachableEntity
        implements FileEmbedModule {
    
    @PostLoad
    private void init() {
        // 접속IP 문자열 목록 세팅
        this.allowedIpStrList = this.allowedIpList.stream()
                .map(UserAllowedIpEntity::getAllowedIp)
                .collect(Collectors.toList());
    }
    
    /** 사용자 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("사용자 ID")
    private Integer id;

    /** 사용자 아이디 */
    @Column(name = "username", length = 20, unique = true)
    @Comment("사용자 아이디")
    private String username;

    /** 비밀번호 :: 암호화된 비밀번호(64bit)를 저장하기 위해 길이=64이다. */
    @Column(name = "password", length = 64)
    @Comment("비밀번호")
    private String password;

    /** 사용자 권한 정보 */
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    @BatchSize(size = 10)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("사용자 권한 정보")
    private List<UserAuthRoleEntity> authList;

    /** 접속 IP 사용 여부 (Y/N) */
    @Builder.Default
    @Column(name = "use_allowed_ip_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("접속 IP 사용 여부")
    private String useAllowedIpYn = "N";

    /** 접속 IP 정보 */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 10)
    @OrderBy("allowedIp ASC")
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("접속 IP 정보")
    private List<UserAllowedIpEntity> allowedIpList;
    
    /** 접속 IP 문자열 목록 */
    @Transient
    private List<String> allowedIpStrList;

    /** 표시이름 : 사용자 프로필 정보가 없을 때 표시되는 이름 */
    @Column(name = "nick_nm", length = 50)
    @Comment("표시이름")
    private String nickNm;

    /** Email 주소 */
    @Column(name = "email", length = 100)
    @Comment("Email 주소")
    private String email;

    /** 연락처 */
    @Column(name = "cttpc", length = 20)
    @Comment("연락처")
    private String cttpc;

    /** 프로필 이미지 URL */
    @Column(name = "profl_img_url", length = 1000)
    @Comment("프로필 이미지 URL")
    private String proflImgUrl;

    /** 계정 설명 (관리자용) */
    @Column(name = "content")
    @Comment("계정 설명")
    private String content;

    /** 사용자 프로필 정보 */
    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("사용자 프로필 정보")
    private UserProfileEntity profile;

    /** 사용자 인사정보 */
    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("사용자 프로필 정보")
    private UserEmplymEntity emplym;

    /** 계정 상태 정보 */
    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("계정 상태 정보")
    public UserStateEntity acntStus;

    /* ----- */

    /**
     * tagify 문자열로부터 접속 가능 IP 목록 세팅
     *
     * @param authStr 쉼표(,)로 구분된 권한 정보 문자열
     */
    public void setAuthList(final String authStr) {
        if (StringUtils.isEmpty(authStr)) return;
        // 권한 정보 문자열에서 권한 목록 생성
        final List<String> authStrList = List.of(authStr.split(","));
        this.setAuthList(authStrList.stream()
                .map(UserAuthRoleEntity::new)
                .collect(Collectors.toList()));
    }

    /**
     * tagify 문자열로부터 접속 가능 IP 목록 세팅
     *
     * @param tagifyStr tagify 형식으로 전달된 IP 주소 문자열
     */
    public void setAllowedIpList(final String tagifyStr) {
        final List<String> allowedIpStrList = CmmUtils.parseTagify(tagifyStr);
        this.setAllowedIpList(allowedIpStrList.stream()
                .map(UserAllowedIpEntity::new)
                .collect(Collectors.toList()));
    }

    /**
     * 서브엔티티 List 처리를 위한 Setter Override
     * 한 번 Entity가 생성된 이후부터는 새 List를 할당하면 안 되고 계속 JPA 이력이 추적되어야 한다.
     *
     * @param allowedIpList - 설정할 객체 리스트
     */
    public void setAllowedIpList(final List<UserAllowedIpEntity> allowedIpList) {
        if (CollectionUtils.isEmpty(allowedIpList)) return;
        if (this.allowedIpList == null) {
            this.allowedIpList = new ArrayList<>(allowedIpList);
        } else {
            this.allowedIpList.clear();
            this.allowedIpList.addAll(allowedIpList);
        }
    }

    /**
     * 서브엔티티 List 처리를 위한 Setter Override
     * 한 번 Entity가 생성된 이후부터는 새 List를 할당하면 안 되고 계속 JPA 이력이 추적되어야 한다.
     *
     * @param authList - 설정할 객체 리스트
     */
    public void setAuthList(final List<UserAuthRoleEntity> authList) {
        if (CollectionUtils.isEmpty(authList)) return;
        if (this.authList == null) {
            this.authList = new ArrayList<>(authList);
        } else {
            this.authList.clear();
            this.authList.addAll(authList);
        }
    }

    /**
     * 사용자 프로필 정보를 업데이트하여 반환합니다.
     *
     * @param dto 업데이트할 사용자 프로필 정보가 담긴 Dto
     * @return {@link UserProfileEntity} -- 업데이트된 사용자 프로필 엔티티
     */
    public UserProfileEntity getProfileUpdt(UserProfileDto dto) throws Exception {
        UserProfileMapstruct.INSTANCE.updateFromDto(dto, this.profile);
        return this.profile;
    }

    /**
     * 사용자 직원정보를 업데이트하여 반환합니다.
     *
     * @param dto 업데이트할 사용자 고용 정보가 담긴 Dto
     * @return {@link UserEmplymEntity} -- 업데이트된 사용자 고용 엔티티
     */
    public UserEmplymEntity getEmplymUpdt(UserEmplymDto dto) throws Exception {
        UserEmplymMapstruct.INSTANCE.updateFromDto(dto, this.emplym);
        return this.emplym;
    }

    /**
     * 등록 시 계층적으로 연관된 엔티티를 cascade.
     */
    public void cascade() {
        if (this.profile != null) this.profile.setUser(this);
        if (this.emplym != null) this.emplym.setUser(this);
        if (this.acntStus != null) this.acntStus.setUser(this);
    }

    public String getRefreshTokenHash() {
        return this.acntStus == null ? null : this.acntStus.getRefreshTokenHash();
    }

    public void setRefreshTokenHash(final String refreshTokenHash) {
        if (this.acntStus == null) this.acntStus = UserStateEntity.builder().build();
        this.acntStus.setRefreshTokenHash(refreshTokenHash);
    }

    public java.util.Date getRefreshTokenIssuedAt() {
        return this.acntStus == null ? null : this.acntStus.getRefreshTokenIssuedAt();
    }

    public void setRefreshTokenIssuedAt(final java.util.Date refreshTokenIssuedAt) {
        if (this.acntStus == null) this.acntStus = UserStateEntity.builder().build();
        this.acntStus.setRefreshTokenIssuedAt(refreshTokenIssuedAt);
    }

    public java.util.Date getRefreshTokenExpiresAt() {
        return this.acntStus == null ? null : this.acntStus.getRefreshTokenExpiresAt();
    }

    public void setRefreshTokenExpiresAt(final java.util.Date refreshTokenExpiresAt) {
        if (this.acntStus == null) this.acntStus = UserStateEntity.builder().build();
        this.acntStus.setRefreshTokenExpiresAt(refreshTokenExpiresAt);
    }

    /* ----- */

    @Embedded
    public FileEmbed file;
}
