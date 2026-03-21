package io.nicheblog.dreamdiary.feature.admin.lgnPolicy.entity;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseAuditEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;

/**
 * LgnPolicyEntity
 * <pre>
 *  로그인 정책 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "lgn_policy")
@DynamicInsert      // null인 값은 (null로 insert하는 대신) insert에서 제외
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = false)
public class LgnPolicyEntity
        extends BaseAuditEntity {

    /** 로그인 정책 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lgn_policy_no")
    @Comment("로그인 정책 번호 (key)")
    private Integer lgnPolicyNo;

    /** 로그인 최대 시도 횟수 */
    @Column(name = "lgn_try_lmt")
    @Comment("로그인 최대 시도 횟수")
    private Integer lgnTryLmt;

    /** 비밀번호 변경 일자 */
    @Column(name = "pw_chg_dy")
    @Comment("비밀번호 변경 일자")
    private Integer pwChgDy;

    /** 미접속시 계정 잠금 일자 */
    @Column(name = "lgn_lock_dy")
    @Comment("미접속시 계정 잠금 일자")
    private Integer lgnLockDy;

    /** 비밀번호 초기화 값 */
    @Column(name = "pw_for_reset", length = 20)
    @Comment("비밀번호 초기화 값")
    private String pwForReset;
}
