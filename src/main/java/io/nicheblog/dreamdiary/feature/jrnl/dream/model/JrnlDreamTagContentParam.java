package io.nicheblog.dreamdiary.feature.jrnl.dream.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * JrnlDreamTagContentParam
 * <pre>
 *  꿈 태그-콘텐츠 목록 검색 파라미터.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString
public class JrnlDreamTagContentParam
        extends BaseSearchParam {

    /** 참조 글 번호 */
    private Integer refId;

    /** 참조 콘텐츠 타입 */
    private String refContentType;

    /** 태그 ID */
    private Integer tagId;

    /** 연도 */
    private Integer yy;

    /** 월 */
    private Integer mnth;

    /** 주 시작일 */
    private String weekStartDt;

    /** 등록자 ID */
    private String createdBy;

}
