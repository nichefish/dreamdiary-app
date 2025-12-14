package io.nicheblog.dreamdiary.domain.vcatn.stats.model;

import io.nicheblog.dreamdiary.domain.user.info.model.UserDto;
import lombok.*;

/**
 * VcatnStatsDto
 * <pre>
 *  휴가계획서 통계 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class VcatnStatsDto {

    /** userId */
    private String userId;

    /** User */
    private UserDto user;

    /** 산정기준년도 */
    private String statsYy;

    /** 산정기준년도 */
    private String statsDt;

    /** 근속년수 */
    @Builder.Default
    private Integer cnwkYy = 0;

    /** 기본연차 */
    @Builder.Default
    private Integer bsYryc = 0;

    /** 신입 여부 */
    @Builder.Default
    private Boolean isNewbie = false;

    /** 신입연차 */
    @Builder.Default
    private Integer newbieYryc = 0;

    /** 근속 추가연차 */
    @Builder.Default
    private Integer cnwkYryc = 0;

    /** 프로젝트 추가연차 */
    @Builder.Default
    private Integer prjctYryc = 0;

    /** 안식주 */
    @Builder.Default
    private Integer refreshYryc = 0;


    /** 총 연차 (프리마커 오류땜에 프로퍼티 추가) */
    @Builder.Default
    private Double totYryc = 0.0;

    /** 소진 연차 */
    @Builder.Default
    private double exhsYryc = 0.0;

    /* ----- */

    /**
     * Getter :: 사용자 Id
     *
     * @return String - 사용자 정보에서 조회한 ID
     */
    public String getUserId() {
        return this.getUser().getUserId();
    }

    /**
     * Getter :: 총 연차
     *
     * @return double - 계산된 총 연차
     */
    public double getTotYryc() {
        return (this.bsYryc + this.newbieYryc + this.cnwkYryc + this.prjctYryc + this.refreshYryc);
    }

    /**
     * Getter :: 잔여 연차 (소진연차를 입력받음)
     *
     * @param exhsYryc - 소진된 연차
     * @return double - 계산된 잔여 연차
     */
    public double getRemndrYryc(final Double exhsYryc) {
        this.exhsYryc = exhsYryc;
        return this.getRemndrYryc();
    }

    /**
     * Getter :: 잔여 연차 (소진연차를 입력받지 않고 자체 정보로 계산)
     *
     * @return double - 계산된 잔여 연차
     */
    public double getRemndrYryc() {
        return (this.getTotYryc() - this.exhsYryc);
    }
}
