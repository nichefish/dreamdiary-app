package io.nicheblog.dreamdiary.domain.vcatn.stats.entity;

import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * VcatnStatsEntity
 * <pre>
 *  휴가관리 > 년도별 휴가관리 년도 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "vcatn_stats_yy")
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class VcatnStatsYyEntity {

    /** 년도 */
    @Id
    @Column(name = "stats_yy")
    @Comment("년도")
    private String statsYy;

    /** 시작일 */
    @Column(name = "bgn_dt")
    @Comment("시작일")
    private Date bgnDt;

    /** 종료일 */
    @Column(name = "end_dt")
    @Comment("종료일")
    private Date endDt;

    /* ----- */

    /**
     * 생성자 = 년도 받아서 기본시작일/종료일(1월 1일~12월 31일) 세팅
     *
     * @param yyStr - 휴가 통계 연도를 나타내는 문자열 (예: "2024")
     */
    public VcatnStatsYyEntity(final String yyStr) throws Exception {
        this.statsYy = yyStr;
        this.bgnDt = DateUtils.asDate(yyStr + "-01-01");
        this.endDt = DateUtils.asDate(yyStr + "-12-31");
    }
}
