package io.nicheblog.dreamdiary.feature.user.signup.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

import java.time.LocalDateTime;

/**
 * UserSignupRequestEntity
 * <pre>
 *  사용자 계정 신청 정보 Entity (`user_signup_request`).
 * </pre>
 *
 * 명명 규약: 테이블·영속 레코드는 {@code UserSignupRequest*}; 유스케이스·화면 진입은 {@code UserSignup*} 로 구분한다.
 */
@Entity
@Table(name = "user_signup_request")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE user_signup_request SET deleted_at = NOW() WHERE id = ?")
public class UserSignupRequestEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "username", length = 20)
    private String username;

    @Column(name = "password", length = 64)
    private String password;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "content")
    private String content;

    /** PENDING/APPROVED/REJECTED */
    @Builder.Default
    @Column(name = "status", length = 20)
    private String status = "PENDING";

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;
}
