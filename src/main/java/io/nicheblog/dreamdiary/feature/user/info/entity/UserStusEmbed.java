package io.nicheblog.dreamdiary.feature.user.info.entity;

import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Date;

/**
 * UserStusInfo
 * <pre>
 *  사용자user에서 계정 상태 관련 정보 분리
 * </pre>
 *
 * @author nichefish
 */
@Embeddable
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStusEmbed {

    /** 잠금 여부 (Y/N) */
    @Builder.Default
    @Column(name = "locked_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private String lockedYn = "Y";

    /** 마지막 로그인 일시 */
    @DateTimeFormat(pattern = DateUtils.PTN_DATETIME)
    @Column(name = "last_login_at")
    @Comment("마지막 로그인 일시")
    private Date lastLoginAt;

    /** 로그인 실패 횟수 */
    @Builder.Default
    @Column(name = "lgn_fail_cnt", columnDefinition = "INT DEFAULT 0")
    @Comment("로그인 실패 횟수")
    private Integer lgnFailCnt = 0;

    /** 패스워드 변경일시 */
    @DateTimeFormat(pattern = DateUtils.PTN_DATETIME)
    @Column(name = "password_changed_at")
    @Comment("패스워드 변경일시")
    private Date passwordChangedAt;

    /** 패스워드 리셋 필요 여부 (Y/N) */
    @Builder.Default
    @Column(name = "needs_pw_reset", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("패스워드 리셋 필요여부")
    private String needsPwReset = "N";

    /** 장기 미로그인 패스 체크 여부 (Y/N) */
    @Builder.Default
    @Column(name = "dormant_bypass_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("장기 미로그인 패스 체크 여부")
    private String dormantBypassYn = "N";

    /** 본인신청 여부 (Y/N) */
    @Builder.Default
    @Column(name = "reqst_yn", length = 1, columnDefinition = "CHAR DEFAULT 'Y'")
    @Comment("본인신청여부")
    private String reqstYn = "Y";

    /** 승인 여부 (Y/N) */
    @Builder.Default
    @Column(name = "cf_yn", length = 1, columnDefinition = "CHAR DEFAULT 'Y'")
    @Comment("승인여부")
    private String cfYn = "Y";

    /* ----- */

    /**
     * 생성자.
     *
     * @param reqstYn 요청 여부 ("Y" 또는 "N")
     * @param cfYn 확인 여부 ("Y" 또는 "N")
     */
    public UserStusEmbed(final String reqstYn, final String cfYn) {
        this.reqstYn = reqstYn;
        this.cfYn = cfYn;
    }

    /**
     * 갓 요청한 상태 = 요청 상태가 "Y", 확인 상태가 "N"인 상태 각체 반환
     *
     * @return {@link UserStusEmbed} -- 상태 객체
     */
    public static UserStusEmbed getReqstStus() {
        return new UserStusEmbed("Y", "N");
    }

    /**
     * 갓 등록한 상태 = 요청 상태가 "N", 확인 상태가 "Y"인 상태 각체 반환
     *
     * @return {@link UserStusEmbed} -- 상태 객체
     */
    public static UserStusEmbed getRegistStus() {
        return new UserStusEmbed("N", "Y");
    }
}
