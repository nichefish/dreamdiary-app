package io.nicheblog.dreamdiary.feature.user.info.model;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.file.model.cmpstn.FileCmpstn;
import io.nicheblog.dreamdiary.feature.file.model.cmpstn.FileCmpstnModule;
import io.nicheblog.dreamdiary.feature.user.info.model.emplym.UserEmplymDto;
import io.nicheblog.dreamdiary.feature.user.info.model.profile.UserProfileDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.validator.state.UpdateState;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UserDto
 * <pre>
 *  사용자(계정) 정보 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserDto
        extends BaseAttachableDto
        implements Identifiable<Integer>, FileCmpstnModule {

    /** 아이디 */
    @NotEmpty
    private String username;
    /** 표시이름 */
    @NotEmpty
    private String nickname;
    /** 프로필 이미지 URL */
    private String profileImageUrl;

    /** 이메일 */
    private String email;
    /** E-mail ID */
    @NotEmpty
    private String emailId;
    /** E-mail 뒷부분 */
    @NotEmpty
    private String emailDomain;
    /** 연락처 */
    private String phoneNumber;

    /** 사용자에게 부여된 역할 목록 */
    private List<UserRoleDto> userRoles;
    /** 부여 역할 키 목록 (표시/검증용) */
    private List<String> roleKeyList;

    /** 사용자 정보 (위임) */
    private UserProfileDto profile;
    /** 사용자 정보 (위임) */
    private UserEmplymDto emplym;

    /** 잠금 여부 (Y/N) */
    @Builder.Default
    @Pattern(regexp = "^[YN]$", groups = UpdateState.class)
    private String lockedYn = "N";

    /** 퇴사 여부 (Y/N) */
    @Builder.Default
    @Pattern(regexp = "^[YN]$", groups = UpdateState.class)
    private String retireYn = "N";

    /** 퇴사일 */
    private String retireDt;

    /** 비밀번호 */
    private String password;

    /** 역할 multiselect 원문 (쉼표 구분 role_key) */
    @NotEmpty
    private String roleKeysStr;

    /** 계정 설명 (관리자용) */
    private String content;

    /** 입사일 */
    private String ecnyDt;
    /** 이름 */
    private String userNm;

    /** 접속IP 사용 여부 체크 */
    @Builder.Default
    private Boolean useAllowedIp = false;

    /** 접속 IP 사용 여부 (Y/N) */
    @Builder.Default
    @Pattern(regexp = "^[YN]$", groups = UpdateState.class)
    private String useAllowedIpYn = "N";

    /** 접속 IP 정보 */
    private String allowedIpListStr;

    /** 접속 IP 정보 */
    private List<UserAllowedIpDto> allowedIpList;

    /** 이메일 반환 (override) */
    public String getEmail() {
        if (!StringUtils.isEmpty(this.email)) return this.email;
        if (!StringUtils.isEmpty(this.emailId) || !StringUtils.isEmpty(this.emailDomain)) return null;
        return this.emailId + "@" + this.emailDomain;
    }

    /** Getter Override — 폼/검증에서 사용하는 역할 키 CSV */
    public String getRoleKeysStr() {
        if (this.userRoles != null) return this.userRoles.stream()
                .map(UserRoleDto::getRoleKey)
                .collect(Collectors.joining(","));
        return this.roleKeysStr;
    }
    
    /* ----- */

    /** Getter :: 잠금여부 채크 */
    public Boolean getIsLocked() {
        return "Y".equals(this.lockedYn);
    }

    /** 내 정보 여부 채크 */
    public Boolean getIsMe() {
        return (AuthUtils.isCreatedBy(this.getUsername()));       // 인자로 넘긴 ID와 세션의 사용자 ID 비교
    }

    @Override
    public Integer getKey() {
        return this.id;
    }

    /* ----- */

    /** 위임 :: 첨부파일 모듈 */
    public FileCmpstn file;
}
