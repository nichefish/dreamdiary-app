package io.nicheblog.dreamdiary.feature.user.profile.entity;

import io.nicheblog.dreamdiary.feature.user.info.entity.UserEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * UserProfileEntity
 * <pre>
 *  사용자 프로필 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "user_profile")
@DynamicInsert
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user"}, callSuper = true)
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE user_profile SET DELETED_AT = 'Y' WHERE id = ?")
public class UserProfileEntity extends BaseCrudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("사용자 프로필 정보 ID")
    private Integer userProfileId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("사용자 정보")
    private UserEntity user;

    @DateTimeFormat(pattern = DateUtils.PTN_DATE)
    @Column(name = "brthdy")
    @Comment("생년월일")
    private Date brthdy;

    @Builder.Default
    @Column(name = "lunar_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("음력여부")
    private String lunarYn = "N";

    @Column(name = "profl_cn", length = 4000)
    @Comment("사용자 설명")
    private String proflCn;
}
