package io.nicheblog.dreamdiary.auth.security.model;

import io.nicheblog.dreamdiary.global.Constant;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AuthUserDto
 * <pre>
 *  Vue SPA 외부 전달용 인증 사용자 정보 DTO.
 *  AuthInfo (Spring Security 내부 객체) 를 직접 직렬화하지 않고 필요한 필드만 추려서 응답한다.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Builder
public class AuthUserDto {

    /** 사용자 ID */
    private String username;

    /** 사용자 이름 */
    private String nickname;

    /** email */
    private String email;

    /** 프로필 이미지 URL */
    private String profileImageUrl;

    /** 부여 역할 목록 */
    private List<RoleDto> roles;

    /** 관리자 여부 */
    private Boolean isMngr;

    /** 개발자 여부 */
    private Boolean isDev;

    /* ----- */

    /**
     * AuthInfo → AuthUserDto 변환.
     *
     * @param authInfo Spring Security 인증 정보
     * @return {@link AuthUserDto} Vue SPA 전달용 DTO
     */
    public static AuthUserDto from(final AuthInfo authInfo) {
        return AuthUserDto.builder()
                .username(authInfo.getUsername())
                .nickname(authInfo.getNickname())
                .email(authInfo.getEmail())
                .profileImageUrl(authInfo.getProfileImageUrl())
                .roles(authInfo.getRoles())
                .isMngr(authInfo.hasAuthority(Constant.ROLE_MNGR))
                .isDev(authInfo.hasAuthority(Constant.ROLE_DEV))
                .build();
    }
}
