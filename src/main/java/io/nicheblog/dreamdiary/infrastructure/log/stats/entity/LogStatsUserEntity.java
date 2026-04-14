package io.nicheblog.dreamdiary.infrastructure.log.stats.entity;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.entity.LogActvtyEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;

/**
 * UserEntity
 * <pre>
 *  계정 정보 Entity :: 사용자 정보 Entity를 위임 필드로 가짐
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "user")
@DynamicInsert      // null인 값은 (null로 insert하는 대신) insert에서 제외
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class LogStatsUserEntity
        extends BaseCrudEntity {

    /** 사용자 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("사용자 번호 (key)")
    private Integer id;

    /** 사용자 아이디 */
    @Column(name = "username", length = 20, unique = true)
    @Comment("사용자 아이디")
    private String username;

    /* ----- */

    /** 활동(접속) 횟수 */
    @Column(name = "actvty_cnt")
    @Comment("활동(접속) 횟수")
    private Long actvtyCnt;

    /** 활동(접속) 목록 */
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "log_user_id", referencedColumnName = "username", insertable = false, updatable = false)
    @Fetch(value = FetchMode.SELECT)
    @BatchSize(size = 10)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("활동(접속) 목록")
    private List<LogActvtyEntity> actvtyList;

    /* ----- */

    /**
     * 생성자.
     *
     * @param username 사용자 계정명
     * @param actvtyCnt 활동 횟수
     */
    public LogStatsUserEntity(final String username, final Long actvtyCnt) {
        this.username = username;
        this.actvtyCnt = actvtyCnt;
    }
}
