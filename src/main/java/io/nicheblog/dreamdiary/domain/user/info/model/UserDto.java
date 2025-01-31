package io.nicheblog.dreamdiary.domain.user.info.model;

import io.nicheblog.dreamdiary.auth.util.AuthUtils;
import io.nicheblog.dreamdiary.domain.user.info.model.emplym.UserEmplymDto;
import io.nicheblog.dreamdiary.domain.user.info.model.profl.UserProflDto;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseAtchDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
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
        extends BaseAtchDto
        implements Identifiable<Integer> {

    /** 사용자 고유 번호 (PK) */
    protected Integer userNo;
    /** 아이디 */
    @NotEmpty
    protected String userId;
    /** 표시이름 */
    @NotEmpty
    protected String nickNm;
    /** 프로필 이미지 URL */
    protected String proflImgUrl;

    /** 이메일 */
    protected String email;
    /** E-mail ID */
    @NotEmpty
    protected String emailId;
    /** E-mail 뒷부분 */
    @NotEmpty
    protected String emailDomain;
    /** 연락처 */
    protected String cttpc;

    /** 사용자 권한 정보 */
    protected List<UserAuthRoleDto> authList;
    /** 사용자 권한 정보(문자열) */
    protected List<String> authStrList;

    /** 사용자 정보 (위임) */
    protected UserProflDto profl;
    /** 사용자 정보 (위임) */
    protected UserEmplymDto emplym;

    /** 본인신청 여부 (Y/N) */
    protected String isReqst;
    /** 승인 여부 */
    protected Boolean isCf;

    /** 잠금 여부 (Y/N) */
    @Builder.Default
    @Size(min = 1, max = 1)
    @Pattern(regexp = "^[YN]$")
    protected String lockedYn = "N";

    /** 퇴사 여부 (Y/N) */
    @Builder.Default
    @Size(min = 1, max = 1)
    @Pattern(regexp = "^[YN]$")
    protected String retireYn = "N";

    /** 퇴사일 */
    protected String retireDt;
    
    /* ----- */

    /** Getter :: 잠금여부 채크 */
    public Boolean getIsLocked() {
        return "Y".equals(this.lockedYn);
    }

    /* ----- */

    /**
     * 사용자(계정) 정보 상세 (DTL) Dto.
     */
    @Getter
    @Setter
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class DTL
            extends UserDto {
        /** 비밀번호 */
        private String password;

        /** 사용자 권한 정보(문자열) (multiselect parameter) */
        @NotEmpty
        private String authListStr;

        /** 계정 설명 (관리자용) */
        private String cn;

        /** 접속IP 사용 여부 체크 */
        @Builder.Default
        private Boolean useAcsIp = false;

        /** 접속 IP 사용 여부 (Y/N) */
        @Builder.Default
        @Size(min = 1, max = 1)
        @Pattern(regexp = "^[YN]$")
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
    }

    /**
     * 사용자(계정) 정보 목록 조회 (LIST) Dto.
     */
    @Getter
    @Setter
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class LIST
            extends UserDto {

        /** 입사일 */
        private String ecnyDt;
        /** 이름 */
        private String userNm;

        /* ----- */

        /** 내 정보 여부 채크 */
        public Boolean getIsMe() {
            return (AuthUtils.isRegstr(this.getUserId()));       // 인자로 넘긴 ID와 세션의 사용자 ID 비교
        }
    }

    @Override
    public Integer getKey() {
        return this.userNo;
    }
}
