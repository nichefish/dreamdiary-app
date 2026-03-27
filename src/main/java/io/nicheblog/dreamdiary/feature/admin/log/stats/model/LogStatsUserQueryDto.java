package io.nicheblog.dreamdiary.feature.admin.log.stats.model;

import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * LogStatsUserQueryDto
 * <pre>
 *  사용자 로그 통계 조회 전용 DTO.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LogStatsUserQueryDto
        extends BaseCrudDto
        implements Comparable<LogStatsUserQueryDto> {

    /** 목록 순번 */
    private Long rnum;
    /** 아이디 */
    private String userId;
    /** 이름 */
    private String userNm;
    /** 권한코드 */
    private String authCd;
    /** 권한이름 */
    private String authNm;
    /** 로그 목록 건수 */
    private Long actvtyCnt;
    /** 사용자 정보 */
    private String userInfoNo;
    /** 퇴직 여부 (Y/N) */
    private String retireYn;

    /* ----- */

    /**
     * 로그 수 기준 정렬 (내림차순)
     *
     * @param other 비교할 객체
     * @return 양수: 현재 객체가 더 큼, 음수: 현재 객체가 더 작음, 0: 두 객체가 같음
     */
    @SneakyThrows
    @Override
    public int compareTo(final @NotNull LogStatsUserQueryDto other) {
        final Long compareCnt = other.getActvtyCnt();
        if (compareCnt == null) return -1;

        return compareCnt.compareTo(this.actvtyCnt);
    }
}
