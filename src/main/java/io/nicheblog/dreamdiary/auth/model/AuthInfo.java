package io.nicheblog.dreamdiary.auth.model;

import io.nicheblog.dreamdiary.domain.user.info.model.profl.UserProflDto;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.CollectionUtils;

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
@EqualsAndHashCode(of = {"userId"}, callSuper = false)
public class AuthInfo
        implements UserDetails, OAuth2User {

    /** 사용자 ID */
    private String userId;

    /** 사용자 PW */
    private String password;

    /** 권한 목록 */
    private List<AuthRoleDto> authList;

    /** 사용자 이름 */
    private String nickNm;

    /** 프로필 이미지 URL */
    private String proflImgUrl;

    /** email */
    private String email;

    /** 승인 여부 (Y/N) */
    private String cfYn;

    /** 잠금 여부 (Y/N) */
    private String lockedYn;

    /** 접속 IP 사용 여부 (Y/N) */
    private String useAcsIpYn;

    /** 접속 IP 목록 */
    private List<String> acsIpStrList;

    /** 최종접속일시 */
    private Date lstLgnDt;

    /** 최종비밀번호변경일시 */
    private Date pwChgDt;

    /** 패스워드 리셋 필요 여부 (Y/N) */
    private String needsPwReset;

    /** 사용자 정보 ID */
    private Integer userProflNo;
    /** 사용자 정보 통으로 저장 (일단) */
    private UserProflDto profl;

    /* ----- */

    /**
     * Getter :: 사용자 프로필 정보
     */
    public String getProflImgUrl() {
        if (StringUtils.isEmpty(this.proflImgUrl)) return (Constant.BLANK_AVATAR_URL);
        return this.proflImgUrl;
    }

    /**
     * Getter :: 사용자 프로필 정보 존재 여부 (내부사용자)
     */
    public Boolean getHasUserProfl() {
        return this.profl != null && this.userProflNo != null;
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
        if (CollectionUtils.isEmpty(this.authList)) throw new RuntimeException(MessageUtils.getMessage("user.auth.empty"));

        return this.authList.stream()
                .map(entity -> {
                    try {
                        if (Constant.AUTH_DEV.equals(entity.getAuthCd())) return new SimpleGrantedAuthority(Constant.ROLE_MNGR);
                        return new SimpleGrantedAuthority("ROLE_" + entity.getAuthCd());
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
        return this.userId;
    }

    /**
     * 사용자 고유 식별자를 조회한다.
     */
    @Override
    public String getName() {
        return this.userId;
    }

    /**
     * 계정 활성(비잠금)여부를 체크한다.
     */
    @Override
    public boolean isAccountNonLocked() {
        return !"Y".equals(this.getLockedYn());
    }

    /**
     * 계정 활성(미정지)여부를 체크한다.
     */
    @Override
    public boolean isEnabled() {
        return !"Y".equals(this.getCfYn());
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
     * UsernamePasswordAuthenticationToken 생성
     */
    public UsernamePasswordAuthenticationToken getAuthToken() {
        return new UsernamePasswordAuthenticationToken(this, this.password, this.getAuthorities());
    }
}
