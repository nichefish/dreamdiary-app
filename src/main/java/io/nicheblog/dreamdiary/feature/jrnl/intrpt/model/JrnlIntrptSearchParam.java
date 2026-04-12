package io.nicheblog.dreamdiary.feature.jrnl.intrpt.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * JrnlIntrptSearchParam
 * <pre>
 *  저널 해석 목록 검색 파라미터.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class JrnlIntrptSearchParam
        extends BaseSearchParam {

    /** 년도 */
    private Integer yy;
    /** 월 */
    private Integer mnth;

    /** 저널 꿈 번호 */
    private Integer jrnlDreamNo;

    /** 컨텐츠 타입 */
    private String contentType;

    /** 항목 키워드 */
    private String dreamKeyword;

    /** 태그 ID */
    private Integer tagId;

    /** 중요 여부 **/
    private String imprtcYn;
}
