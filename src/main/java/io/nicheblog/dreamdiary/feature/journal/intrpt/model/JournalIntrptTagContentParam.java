package io.nicheblog.dreamdiary.feature.journal.intrpt.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * JournalIntrptTagContentParam
 * <pre>
 *  저널 해석 태그-컨텐츠 목록 검색 파라미터.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString
public class JournalIntrptTagContentParam
        extends BaseSearchParam {

    /** 참조 글 번호 */
    private Integer refId;

    /** 참조 컨텐츠 타입 */
    private String refContentType;

    /** 태그 ID */
    private Integer tagId;

    /** 년도 */
    private Integer yy;

    /** 월 */
    private Integer mnth;

    /** 등록자 ID */
    private String createdBy;

}
