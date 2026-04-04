package io.nicheblog.dreamdiary.feature.clsf.history.model.cmpstn;

import io.nicheblog.dreamdiary.auth.security.model.AuditorDto;
import lombok.*;

import java.io.Serializable;

/**
 * HistoryCmpstn
 * <pre>
 *  임베드 :: 마지막 본문 수정 정보. (dto level)
 * </pre>
 *
 * @author nichefish
 * @see HistoryCmpstnModule
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryCmpstn
        implements Serializable {

    /** 마지막 본문 수정자 ID */
    private String historyTriggeredBy;

    /** 마지막 본문 수정자 이름 */
    private String historyTriggeredByNm;

    /** 마지막 본문 수정자 정보 */
    private AuditorDto historyTriggeredByInfo;

    /** 현재 로그인 사용자의 마지막 본문 수정 여부 */
    private Boolean isHistoryTriggeredBy;

    /** 마지막 본문 수정일시 */
    private String historyTriggeredAt;
}
