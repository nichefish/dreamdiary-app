package io.nicheblog.dreamdiary.domain.user.info.entity;

import io.nicheblog.dreamdiary.domain.user.emplym.entity.UserEmplymEntity;
import io.nicheblog.dreamdiary.domain.user.emplym.mapstruct.UserEmplymMapstruct;
import io.nicheblog.dreamdiary.domain.user.info.model.emplym.UserEmplymDto;
import io.nicheblog.dreamdiary.domain.user.info.model.profl.UserProflDto;
import io.nicheblog.dreamdiary.domain.user.profl.entity.UserProflEntity;
import io.nicheblog.dreamdiary.domain.user.profl.mapstruct.UserProflMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseAtchEntity;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.*;
import org.springframework.util.CollectionUtils;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.*;
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
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE user SET del_yn = 'Y' WHERE user_no = ?")
public class UserEntity
        extends BaseAtchEntity {
    
    @PostLoad
    private void init() {
        // 접속IP 문자열 목록 세팅
        this.acsIpStrList = this.acsIpList.stream()
                .map(UserAcsIpEntity::getAcsIp)
                .collect(Collectors.toList());
    }
    
    /** 사용자 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_no")
    @Comment("사용자 번호 (PK)")
    private Integer userNo;

    /** 사용자 아이디 */
    @Column(name = "user_id", length = 20, unique = true)
    @Comment("사용자 아이디")
    private String userId;

    /** 비밀번호 :: 암호화된 비밀번호(64bit)를 저장하기 위해 길이=64이다. */
    @Column(name = "password", length = 64)
    @Comment("비밀번호")
    private String password;

    /** 사용자 권한 정보 */
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_no")
    @BatchSize(size = 10)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("사용자 권한 정보")
    private List<UserAuthRoleEntity> authList;

    /** 접속 IP 사용 여부 (Y/N) */
    @Builder.Default
    @Column(name = "use_acs_ip_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("접속 IP 사용 여부")
    private String useAcsIpYn = "N";

    /** 접속 IP 정보 */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_no")
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 10)
    @OrderBy("acsIp ASC")
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("접속 IP 정보")
    private List<UserAcsIpEntity> acsIpList;
    
    /** 접속 IP 문자열 목록 */
    @Transient
    private List<String> acsIpStrList;

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
    @Column(name = "cn")
    @Comment("계정 설명")
    private String cn;

    /** 사용자 프로필 정보 */
    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_no")
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("사용자 프로필 정보")
    private UserProflEntity profl;

    /** 사용자 인사정보 */
    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_no", referencedColumnName = "user_no")
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("사용자 프로필 정보")
    private UserEmplymEntity emplym;

    /** 계정 상태 정보 (위임) */
    @Embedded
    public UserStusEmbed acntStus;

    /* ----- */

    /**
     * tagify 문자열로부터 List<useAcsIpEntity> 세팅
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
     * tagify 문자열로부터 List<useAcsIpEntity> 세팅
     *
     * @param tagifyStr tagify 형식으로 전달된 IP 주소 문자열
     */
    public void setAcsIpList(final String tagifyStr) {
        final List<String> acsIpStrList = CmmUtils.parseTagify(tagifyStr);
        this.setAcsIpList(acsIpStrList.stream()
                .map(UserAcsIpEntity::new)
                .collect(Collectors.toList()));
    }

    /**
     * 서브엔티티 List 처리를 위한 Setter Override
     * 한 번 Entity가 생성된 이후부터는 새 List를 할당하면 안 되고 계속 JPA 이력이 추적되어야 한다.
     *
     * @param acsIpList - 설정할 객체 리스트
     */
    public void setAcsIpList(final List<UserAcsIpEntity> acsIpList) {
        if (CollectionUtils.isEmpty(acsIpList)) return;
        if (this.acsIpList == null) {
            this.acsIpList = new ArrayList<>(acsIpList);
        } else {
            this.acsIpList.clear();
            this.acsIpList.addAll(acsIpList);
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
     * @return {@link UserProflEntity} -- 업데이트된 사용자 프로필 엔티티
     * @throws Exception 업데이트 과정에서 발생할 수 있는 예외
     */
    public UserProflEntity getProflUpdt(UserProflDto dto) throws Exception {
        UserProflMapstruct.INSTANCE.updateFromDto(dto, this.profl);
        return this.profl;
    }

    /**
     * 사용자 직원정보를 업데이트하여 반환합니다.
     *
     * @param dto 업데이트할 사용자 고용 정보가 담긴 Dto
     * @return {@link UserEmplymEntity} -- 업데이트된 사용자 고용 엔티티
     * @throws Exception 업데이트 과정에서 발생할 수 있는 예외
     */
    public UserEmplymEntity getEmplymUpdt(UserEmplymDto dto) throws Exception {
        UserEmplymMapstruct.INSTANCE.updateFromDto(dto, this.emplym);
        return this.emplym;
    }

    /**
     * 등록 시 계층적으로 연관된 엔티티를 cascade.
     */
    public void cascade() {
        if (this.profl != null) this.profl.setUser(this);
        if (this.emplym != null) this.emplym.setUser(this);
    }
}