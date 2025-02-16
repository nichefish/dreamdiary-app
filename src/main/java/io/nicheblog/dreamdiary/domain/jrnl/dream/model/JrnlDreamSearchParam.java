package io.nicheblog.dreamdiary.domain.jrnl.dream.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * JrnlDreamSearchParam
 * <pre>
 *  저널 꿈 목록 검색 파라미터.
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
public class JrnlDreamSearchParam
        extends BaseSearchParam {

    /** 년도 */
    private Integer yy;

    /** 월 */
    private Integer mnth;

    /** 저널 일자 번호 */
    private Integer jrnlDayNo;

    /** 컨텐츠 타입 */
    private String contentType;

    /** 꿈 키워드 */
    private String dreamKeyword;

    /** 태그 번호 */
    private Integer tagNo;

    /** 중요 여부 **/
    private String imprtcYn;
}
