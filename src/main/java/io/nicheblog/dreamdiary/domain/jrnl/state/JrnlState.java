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
    private String collapseYn;
    /** 중요 여부(Y/N) */
    private String imprtcYn;

    /**
     * JrnlState 생성 팩토리 메서드
     *
     * @param resolvedYn resolved 여부(DB 값, 문자열 or null)
     * @param collapseYn collapse 여부(DB 값, 문자열 or null)
     * @param imprtcYn imprtc 여부(DB 값, 문자열 or null)
     * @return 인스턴스
     */
    public static JrnlState of(final String resolvedYn, final String collapseYn, final String imprtcYn) {
        return JrnlState.builder()
                .resolvedYn(resolvedYn)
                .collapseYn(collapseYn)
                .imprtcYn(imprtcYn)
                .build();
    }
}
