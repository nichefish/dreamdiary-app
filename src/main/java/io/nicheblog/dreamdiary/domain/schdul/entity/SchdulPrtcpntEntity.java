package io.nicheblog.dreamdiary.domain.schdul.entity;

import io.nicheblog.dreamdiary.auth.entity.AuditorInfo;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;

/**
 * SchdulPrtcpntEntity
 * <pre>
 *  일정 참여자 Entity
 *  ※일정 참여자(schdul_prtcpnt) = 일정(schdul)에 N:1로 귀속된다.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "schdul_prtcpnt")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE schdul_prtcpnt SET del_yn = 'Y' WHERE schdul_prtcpnt_no = ?")
public class SchdulPrtcpntEntity
        extends BaseCrudEntity {

    /** 일정 참여자 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schdul_prtcpnt_no")
    @Comment("일정 참여자 번호 (PK)")
    private Integer schdulPrtcpntNo;

    /** 일정 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ref_post_no", referencedColumnName = "post_no")
    @Fetch(FetchMode.JOIN)
    @Comment("일정 정보")
    private SchdulEntity schdul;

    /** 참여자 ID */
    @Column(name = "user_id", length = 20)
    @Comment("참여자 ID")
    private String userId;

    /** 참여자 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    @Fetch(value = FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("참여자 정보")
    private AuditorInfo user;
}
