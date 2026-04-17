package io.nicheblog.dreamdiary.feature.user.reqst.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

/**
 * UserSignupRequestEntity
 * <pre>
 *  사용자 계정 신청 정보 Entity.
 * </pre>
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

    @Column(name = "nick_nm", length = 50)
    private String nickNm;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "cttpc", length = 20)
    private String cttpc;

    @Column(name = "content")
    private String content;

    /** PENDING/APPROVED/REJECTED */
    @Builder.Default
    @Column(name = "request_status", length = 20)
    private String requestStatus = "PENDING";

    @Column(name = "approved_at")
    private Date approvedAt;

    @Column(name = "rejected_at")
    private Date rejectedAt;
}
