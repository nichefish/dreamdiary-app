package io.nicheblog.dreamdiary.domain.jrnl.state;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * JrnlState
 * 저널 상태 캐시용 객체
 *
 * @author nichefish
 */
@Getter
@Setter
@Builder
public class JrnlState {

    /** 정리완료 여부(Y/N) */
    private String resolvedYn;
    /** 글접기 여부(Y/N) */
    private String collapsedYn;
    /** 중요 여부(Y/N) */
    private String imprtcYn;
    /** 참조 여부(Y/N) */
    private String refrncYn;
}
