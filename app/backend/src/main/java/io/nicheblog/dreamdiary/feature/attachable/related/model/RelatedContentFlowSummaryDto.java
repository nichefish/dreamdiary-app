package io.nicheblog.dreamdiary.feature.attachable.related.model;

import lombok.Builder;
import lombok.Getter;

/**
 * RelatedContentFlowSummaryDto
 * <pre>
 *  목록 엔트리에 병합할 FLOW 연결 컴포넌트 요약.
 * </pre>
 */
@Getter
@Builder
public class RelatedContentFlowSummaryDto {

    /** 연결 컴포넌트에 포함된 유효 엔트리 수 */
    private final int entryCount;

    /** 연결 컴포넌트 내부 직접 FLOW 관계 수 */
    private final int relationCount;

    /** 연결 컴포넌트의 최초 기준일 */
    private final String startStdrdDt;

    /** 연결 컴포넌트의 최종 기준일 */
    private final String endStdrdDt;
}
