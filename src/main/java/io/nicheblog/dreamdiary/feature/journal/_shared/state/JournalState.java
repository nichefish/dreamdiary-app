package io.nicheblog.dreamdiary.feature.journal._shared.state;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * JournalState
 * 저널 상태 캐시용 객체
 *
 * @author nichefish
 */
@Getter
@Setter
@Builder
public class JournalState {

    /** 정리완료 여부 */
    private Boolean resolved;
    /** 글접기 여부 */
    private Boolean collapsed;
    /** 중요 여부 */
    private Boolean imprtc;
    /** 참조 여부 */
    private Boolean refrnc;
}
