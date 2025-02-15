package io.nicheblog.dreamdiary.domain.jrnl.diary.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * JrnlDiarySearchParam
 * <pre>
 *  저널 일기 목록 검색 파라미터.
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
public class JrnlDiarySearchParam
        extends BaseSearchParam {

    /** 년도 */
    private Integer yy;

    /** 월 */
    private Integer mnth;

    /** 저널 일자 번호 */
    private Integer jrnlDayNo;

    /** 컨텐츠 타입 */
    private String contentType;

    /** 일기 키워드 */
    private String diaryKeyword;

    /** 태그 번호 */
    private Integer tagNo;

    /** 중요 여부 **/
    private String imprtcYn;
}
