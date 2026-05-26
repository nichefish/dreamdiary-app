package io.nicheblog.dreamdiary.infrastructure.log.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 통합 로그 목록 검색 파라미터.
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LogSearchParam
        extends BaseSearchParam {

    /** 글분류 코드 */
    private String ctgrCd;

    /** 제목 */
    private String title;

    /** 최소 소요 시간(ms) */
    private Long minDurationMs;

    /** 예외 발생 로그만 조회 */
    private Boolean hasException;
}
