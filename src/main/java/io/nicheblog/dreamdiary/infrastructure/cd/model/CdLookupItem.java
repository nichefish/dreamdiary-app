package io.nicheblog.dreamdiary.infrastructure.cd.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * CdLookupItem.
 * <pre>
 *  코드 조회/캐시 전용 인프라 모델.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CdLookupItem {

    /** 분류 코드 */
    private String clCd;
    /** 상세 코드 */
    private String dtlCd;
    /** 상세 코드명 */
    private String dtlCdNm;
    /** 설명 */
    private String dc;
    /** 정렬 순서 */
    private Integer sortOrder;
    /** 사용 여부 */
    private String useYn;
    /** 보호 여부 */
    private String protectedYn;
}
