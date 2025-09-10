package io.nicheblog.dreamdiary.domain.user.profl.entity;

import io.nicheblog.dreamdiary.domain.user.info.entity.UserEntity;
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
 * UserProflEntity
 * <pre>
 *  사용자 프로필 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "user_profl")
@DynamicInsert      // null인 값은 (null로 insert하는 대신) insert에서 제외
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user"}, callSuper = true)
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE user_profl SET DEL_YN = 'Y' WHERE user_profl_no = ?")
public class UserProflEntity
        extends BaseCrudEntity {

    /** 사용자 프로필 정보 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_profl_no")
    @Comment("사용자 프로필 정보 번호 (PK)")
    private Integer userProflNo;

    /** 사용자 정보 */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_no", referencedColumnName = "user_no")
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("사용자 정보")
    private UserEntity user;

    /** 생년월일 */
    @DateTimeFormat(pattern = DateUtils.PTN_DATE)
    @Column(name = "brthdy")
    @Comment("생년월일")
    private Date brthdy;

    /** 음력 여부 (Y/N) */
    @Builder.Default        // Builder 사용시 초기값 세팅하도록 설정
    @Column(name = "lunar_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("음력여부")
    private String lunarYn = "N";

    /** 사용자 설명 (관리자용) */
    @Column(name = "profl_cn", length = 4000)
    @Comment("사용자 설명")
    private String proflCn;

}