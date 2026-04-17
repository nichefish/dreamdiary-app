package io.nicheblog.dreamdiary.auth.security.model;

import io.nicheblog.dreamdiary.feature.user.info.model.profile.UserProfileDto;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import lombok.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AuthInfo
 * <pre>
 *  Spring Security:: 사용자 인증정보 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"username"}, callSuper = false)
public class AuthInfo
        implements UserDetails, OAuth2User {

    /** 사용자 ID */
    private String username;

    /** 사용자 PW */
    private String password;

    /** Spring Security에 매핑할 부여 역할 목록 (RoleDto) */
    private List<RoleDto> roles;

    /** 사용자 이름 */
    private String nickname;

    /** 프로필 이미지 URL */
    private String profileImageUrl;

    /** email */
    private String email;

    /** 잠금 여부 (Y/N) */
    private String lockedYn;

    /** 접속 IP 사용 여부 (Y/N) */
    private String useAllowedIpYn;

    /** 접속 IP 목록 */
    private List<String> allowedIpStrList;

    /** 최종접속일시 */
    private Date lastLoginAt;

    /** 계정 잠금 만료 시각 */
    private Date lockExpiresAt;

    /** 최종비밀번호변경일시 */
    private Date passwordChangedAt;

    /** 패스워드 리셋 필요 여부 (Y/N) */
    private String needsPasswordReset;

    /** 패스워드 리셋 토큰 발급 시각 */
    private Date passwordResetTokenIssuedAt;

    /** 사용자 정보 ID */
    private Integer userProfileId;
    /** 사용자 정보 통으로 저장 (일단) */
    private UserProfileDto profile;

    /* ----- */

    /**
     * Getter :: 사용자 프로필 정보
     */
    public String getProfileImageUrl() {
        if (StringUtils.isEmpty(this.profileImageUrl)) return (Constant.BLANK_AVATAR_URL);
        return this.profileImageUrl;
    }

    /**
     * Getter :: 사용자 프로필 정보 존재 여부 (내부사용자)
     */
    public Boolean getHasUserProfile() {
        return this.profile != null && this.userProfileId != null;
    }

    /**
     * Getter :: 관리자 여부
     */
    public Boolean getIsMngr() {
        return this.hasAuthority(Constant.ROLE_MNGR);
    }

    /**
     * Getter :: 개발자 여부
     */
    public Boolean getIsDev() {
        return this.hasAuthority(Constant.ROLE_DEV);
    }

    /**
     * ???
     * TODO: 이거 뭐지?
     */
    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    /**
     * 계정 권한 목록 조회.
     *
     * @return {@link Collection} -- 권한 목록
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (CollectionUtils.isEmpty(this.roles)) throw new RuntimeException(MessageUtils.getMessage("msg.user.auth.empty"));

        return this.roles.stream()
                .map(entity -> {
                    try {
                        if (Code.AUTH_DEV.equals(entity.getRoleKey())) return new SimpleGrantedAuthority(Constant.ROLE_MNGR);
                        return new SimpleGrantedAuthority("ROLE_" + entity.getRoleKey());
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 특정 권한 보유 여부를 체크한다.
     *
     * @param roleStr 확인할 권한 문자열
     * @return {@link Boolean} -- 해당 권한 보유시 true, 아니면 false
     */
    public boolean hasAuthority(final String roleStr) {
        for (final GrantedAuthority grantedAuthority : this.getAuthorities()) {
            if (roleStr.equals(grantedAuthority.getAuthority())) return true;
        }
        return false;
    }

    /**
     * 사용자 고유 식별자를 조회한다.
     */
    @Override
    public String getUsername() {
        return this.username;
    }

    /**
     * 사용자 고유 식별자를 조회한다.
     */
    @Override
    public String getName() {
        return this.username;
    }

    /**
     * 계정 활성(비잠금)여부를 체크한다.
     */
    @Override
    public boolean isAccountNonLocked() {
        if (!"Y".equals(this.getLockedYn())) return true;
        if (this.lockExpiresAt == null) return false;
        return this.lockExpiresAt.after(new Date()) ? false : true;
    }

    /**
     * 계정 활성(미정지)여부를 체크한다.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * 계정 활성(미만료)여부를 체크한다. (*not used yet*)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 비밀번호 활성(미만료)여부를 체크한다. (*not used yet*)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 인증객체가 가진 패스워드 정보를 무효화한다.
     */
    public void nullifyPasswordInfo() {
        this.password = null;
    }

    /**
     * 템플릿/헤더용: 첫 번째 부여 역할의 키 (아이콘 분기 등)
     */
    public String getPrimaryRoleKey() {
        if (CollectionUtils.isEmpty(this.roles)) return null;
        return this.roles.get(0).getRoleKey();
    }

    /**
     * UsernamePasswordAuthenticationToken 생성
     */
    public UsernamePasswordAuthenticationToken getAuthToken() {
        return new UsernamePasswordAuthenticationToken(this, this.password, this.getAuthorities());
    }

    public AuthInfo(Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes, String userNameAttributeName) {
        // ===== 기본 식별 =====
        this.username = (String) attributes.get(userNameAttributeName);
        this.email = (String) attributes.get("email");

        // ===== 기본값 세팅 =====
        this.password = null;
        this.nickname = (String) attributes.getOrDefault("name", this.username);
        this.profileImageUrl = (String) attributes.get("profile_image");

        // ===== OAuth 기본 상태 =====
        this.lockedYn = "N";
        this.useAllowedIpYn = "N";
        this.needsPasswordReset = "N";

        // ===== 권한 처리 =====
        this.roles = authorities.stream()
                .map(auth -> RoleDto.builder()
                        .roleKey(auth.getAuthority().replace("ROLE_", ""))
                        .build())
                .collect(Collectors.toList());

        // ===== 기타 =====
        this.allowedIpStrList = List.of();
        this.lastLoginAt = new Date();
        this.passwordChangedAt = null;
    }

}
