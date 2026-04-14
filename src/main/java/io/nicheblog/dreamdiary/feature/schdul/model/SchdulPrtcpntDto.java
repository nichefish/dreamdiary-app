package io.nicheblog.dreamdiary.feature.schdul.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SchdulPrtcpntDto
 * <pre>
 *  일정 참여자 Dto
 *  ※ 일정 참여자(schdul_prtcpnt) = 일정(schdul)에 N:1로 귀속된다.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"username"})
public class SchdulPrtcpntDto {

    /** 일정 참여자 ID */
    private Integer id;

    /** 일정 ID */
    private Integer schdulId;

    /** 참석자 계정명 */
    private String username;

    /** 참석자 이름 */
    private String userNm;

    /* ----- */

    /**
     * 생성자.
     *
     * @param username 사용자 계정명
     */
    public SchdulPrtcpntDto(final String username) {
        this.username = username;
    }

}
