package io.nicheblog.dreamdiary.feature.calendar.schedule.entity;

import io.nicheblog.dreamdiary.auth.security.entity.AuditorInfo;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * SchedulePrtcpntEntity
 * <pre>
 *  일정 참여자 Entity
 *  ※일정 참여자(schedule_participant) = 일정(schedule)에 N:1로 귀속된다.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "schedule_participant")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE schedule_participant SET deleted_at = NOW() WHERE id = ?")
public class SchedulePrtcpntEntity
        extends BaseCrudEntity {

    /** 일정 참여자 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("일정 참여자 ID")
    private Integer id;

    /** 일정 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "schedule_id", referencedColumnName = "id")
    @Fetch(FetchMode.JOIN)
    @Comment("일정 정보")
    private ScheduleEntity schedule;

    /** 참여자 ID */
    @Column(name = "username", length = 20)
    @Comment("참여자 ID")
    private String username;

    /** 참여자 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "username", referencedColumnName = "username", insertable = false, updatable = false)
    @Fetch(value = FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("참여자 정보")
    private AuditorInfo user;
}

