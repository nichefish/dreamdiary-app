package io.nicheblog.dreamdiary.feature.user.account.entity;

import lombok.*;
import org.hibernate.annotations.Comment;

import javax.persistence.*;

import java.time.LocalDateTime;

/**
 * UserPasswordHistoryEntity
 * <pre>
 *  Password history entity for preventing recent password reuse.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "user_password_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordHistoryEntity {

    /** Password history ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("Password history ID")
    private Integer id;

    /** User ID */
    @Column(name = "user_id", nullable = false)
    @Comment("User ID")
    private Integer userId;

    /** Previous password hash */
    @Column(name = "password_hash", nullable = false, length = 64)
    @Comment("Previous password hash")
    private String passwordHash;

    /** Password changed timestamp */
    @Column(name = "changed_at", nullable = false)
    @Comment("Password changed timestamp")
    private LocalDateTime changedAt;
}
