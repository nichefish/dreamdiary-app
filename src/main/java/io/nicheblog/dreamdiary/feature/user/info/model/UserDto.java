package io.nicheblog.dreamdiary.feature.user.info.model;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.model.BaseClsfDto;
import io.nicheblog.dreamdiary.feature.clsf.file.model.cmpstn.AtchFileCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.file.model.cmpstn.AtchFileCmpstnModule;
import io.nicheblog.dreamdiary.feature.user.info.model.emplym.UserEmplymDto;
import io.nicheblog.dreamdiary.feature.user.info.model.profl.UserProflDto;
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
        extends BaseClsfDto
        implements Identifiable<Integer>, AtchFileCmpstnModule {

    /** 사용자 고유 ID */
    private Integer userNo;
    /** 아이디 */
    @NotEmpty
    private String userId;
    /** 표시이름 */
    @NotEmpty
    private String nickNm;
    /** 프로필 이미지 URL */
    private String proflImgUrl;

    /** 이메일 */
    private String email;
    /** E-mail ID */
    @NotEmpty
    private String emailId;
    /** E-mail 뒷부분 */
    @NotEmpty
    private String emailDomain;
    /** 연락처 */
    private String cttpc;

    /** 사용자 권한 정보 */
    private List<UserAuthRoleDto> authList;
    /** 사용자 권한 정보(문자열) */
    private List<String> authStrList;

    /** 사용자 정보 (위임) */
    private UserProflDto profl;
    /** 사용자 정보 (위임) */
    private UserEmplymDto emplym;

    /** 본인신청 여부 (Y/N) */
    private String isReqst;
    /** 승인 여부 */
    private Boolean isCf;

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

    /** 사용자 권한 정보(문자열) (multiselect parameter) */
    @NotEmpty
    private String authListStr;

    /** 계정 설명 (관리자용) */
    private String cn;

    /** 입사일 */
    private String ecnyDt;
    /** 이름 */
    private String userNm;

    /** 접속IP 사용 여부 체크 */
    @Builder.Default
    private Boolean useAcsIp = false;

    /** 접속 IP 사용 여부 (Y/N) */
    @Builder.Default
    @Pattern(regexp = "^[YN]$", groups = UpdateState.class)
    private String useAcsIpYn = "N";

    /** 접속 IP 정보 */
    private String acsIpListStr;

    /** 접속 IP 정보 */
    private List<UserAcsIpDto> acsIpList;

    /** 이메일 반환 (override) */
    public String getEmail() {
        if (!StringUtils.isEmpty(this.email)) return this.email;
        if (!StringUtils.isEmpty(this.emailId) || !StringUtils.isEmpty(this.emailDomain)) return null;
        return this.emailId + "@" + this.emailDomain;
    }

    /** Getter Override */
    public String getAuthListStr() {
        if (this.authList != null) return this.authList.stream()
                .map(UserAuthRoleDto::getAuthCd)
                .collect(Collectors.joining(","));
        return this.authListStr;
    }
    
    /* ----- */

    /** Getter :: 잠금여부 채크 */
    public Boolean getIsLocked() {
        return "Y".equals(this.lockedYn);
    }

    /** 내 정보 여부 채크 */
    public Boolean getIsMe() {
        return (AuthUtils.isRegstr(this.getUserId()));       // 인자로 넘긴 ID와 세션의 사용자 ID 비교
    }

    @Override
    public Integer getKey() {
        return this.userNo;
    }

    /* ----- */

    /** 위임 :: 첨부파일 모듈 */
    public AtchFileCmpstn file;
}
