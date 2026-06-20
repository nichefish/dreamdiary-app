package io.nicheblog.dreamdiary.auth.policy.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;

/**
 * AuthPolicyEntity
 * <pre>
 *  인증 정책 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "auth_policy")
@DynamicInsert      // null인 값은 (null로 insert하는 대신) insert에서 제외
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = false)
public class AuthPolicyEntity
        extends BaseAuditEntity {

    /** 인증 정책 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("인증 정책 번호 (key)")
    private Integer id;

    /** 로그인 시도 제한 횟수 */
    @Column(name = "login_attempt_limit")
    @Comment("로그인 시도 제한 횟수")
    private Integer loginAttemptLimit;

    /** 로그인 시도 누적 시간 창(분) */
    @Column(name = "login_attempt_window_minutes")
    @Comment("로그인 시도 누적 시간 창(분)")
    private Integer loginAttemptWindowMinutes;

    /** 계정 잠금 지속 시간(분) */
    @Column(name = "account_lock_duration_minutes")
    @Comment("계정 잠금 지속 시간(분)")
    private Integer accountLockDurationMinutes;

    /** 사용자 체감 로그인 유지 시간(분) */
    @Column(name = "session_timeout_minutes")
    @Comment("사용자 체감 로그인 유지 시간(분)")
    private Integer sessionTimeoutMinutes;

    /** 비밀번호 변경 주기(일) */
    @Column(name = "password_change_cycle_days")
    @Comment("비밀번호 변경 주기(일)")
    private Integer passwordChangeCycleDays;

    /** 미로그인 시 잠금 일수 */
    @Column(name = "inactive_lock_days")
    @Comment("미로그인 시 잠금 일수")
    private Integer inactiveLockDays;

    /** 비밀번호 재설정 토큰 만료 시간(분) */
    @Column(name = "password_reset_token_expiry_minutes")
    @Comment("비밀번호 재설정 토큰 만료 시간(분)")
    private Integer passwordResetTokenExpiryMinutes;

    /** 중복 로그인 허용 여부 */
    @Column(name = "duplicate_login_allowed_yn", length = 1)
    @Comment("중복 로그인 허용 여부 (Y/N)")
    private String duplicateLoginAllowedYn;
}
