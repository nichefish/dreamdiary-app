package io.nicheblog.dreamdiary.domain.jrnl.day.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * JrnlDayTagContentParam
 * <pre>
 *  저널 일자 태그-컨텐츠 목록 검색 파라미터.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString
public class JrnlDayTagContentParam {

    /** 참조 글 번호 */
    private Integer refPostNo;

    /** 참조 컨텐츠 타입 */
    private String refContentType;

    /** 태그 번호 */
    private Integer tagNo;

    /** 년도 */
    private Integer yy;

    /** 월 */
    private Integer mnth;

    /** 등록자 ID */
    private String regstrId;
}
