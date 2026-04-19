package io.nicheblog.dreamdiary.feature.journal.interpretation.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * JournalInterpretationSearchParam
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
public class JournalInterpretationSearchParam
        extends BaseSearchParam {

    /** 년도 */
    private Integer yy;
    /** 월 */
    private Integer mnth;

    /** 참조 엔티티 번호 */
    private Integer refId;
    /** 참조 컨텐츠 타입 */
    private ContentType refContentType;

    /** 항목 키워드 */
    private String dreamKeyword;

    /** 중요 여부 **/
    private String imprtcYn;
}

