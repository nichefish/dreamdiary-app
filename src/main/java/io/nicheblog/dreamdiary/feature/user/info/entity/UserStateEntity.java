package io.nicheblog.dreamdiary.feature.user.info.entity;

import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

/**
 * UserStateEntity
 * <pre>
 *  사용자 계정 상태(user_state) Entity.
 * </pre>
 */
@Entity
@Table(name = "user_state")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStateEntity {

    /** 사용자 ID (PK/FK) */
    @Id
    @Column(name = "user_id")
    @Comment("사용자 ID")
    private Integer userId;

    /** 사용자 */
    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    /** Refresh Token Hash */
    @Column(name = "refresh_token_hash", length = 64)
    @Comment("Refresh Token Hash")
    private String refreshTokenHash;

    /** Refresh Token 발생일시 */
    @DateTimeFormat(pattern = DateUtils.PTN_DATETIME)
    @Column(name = "refresh_token_issued_at")
    @Comment("Refresh Token 발생일시")
    private Date refreshTokenIssuedAt;

    /** Refresh Token 만료일시 */
    @DateTimeFormat(pattern = DateUtils.PTN_DATETIME)
    @Column(name = "refresh_token_expires_at")
    @Comment("Refresh Token 만료일시")
    private Date refreshTokenExpiresAt;

    /** 잠금 여부 (Y/N) */
    @Builder.Default
    @Column(name = "locked_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String lockedYn = "N";

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

    /** 로그인 실패 카운트 윈도우 시작 시각 */
    @DateTimeFormat(pattern = DateUtils.PTN_DATETIME)
    @Column(name = "lgn_fail_window_started_at")
    @Comment("로그인 실패 카운트 윈도우 시작 시각")
    private Date lgnFailWindowStartedAt;

    /** 계정 잠금 만료 시각 */
    @DateTimeFormat(pattern = DateUtils.PTN_DATETIME)
    @Column(name = "lock_expires_at")
    @Comment("계정 잠금 만료 시각")
    private Date lockExpiresAt;

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

    /** 패스워드 리셋 토큰 발급 시각 */
    @DateTimeFormat(pattern = DateUtils.PTN_DATETIME)
    @Column(name = "pw_reset_token_issued_at")
    @Comment("패스워드 리셋 토큰 발급 시각")
    private Date pwResetTokenIssuedAt;

    /** 장기 미로그인 패스 체크 여부 (Y/N) */
    @Builder.Default
    @Column(name = "dormant_bypass_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("장기 미로그인 패스 체크 여부")
    private String dormantBypassYn = "N";

    public static UserStateEntity getRegistStus() {
        return UserStateEntity.builder()
                .lockedYn("N")
                .lgnFailCnt(0)
                .needsPwReset("N")
                .dormantBypassYn("N")
                .build();
    }
}
