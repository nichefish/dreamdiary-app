package io.nicheblog.dreamdiary.feature.journal.day.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * JournalDayTagContentParam
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
public class JournalDayTagContentParam {

    /** 참조 글 번호 */
    private Integer refId;

    /** 참조 컨텐츠 타입 */
    private String refContentType;

    /** 태그 ID */
    private Integer tagId;

    /** 연도 */
    private Integer yy;

    /** 월 */
    private Integer mnth;

    /** 주 시작일 */
    private String weekStartDt;

    /** 기준일자 */
    private String stdrdDt;

    /** 등록자 ID */
    private String createdBy;

}
