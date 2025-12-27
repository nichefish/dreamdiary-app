package io.nicheblog.dreamdiary.domain.jrnl.day.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * JrnlDaySearchParam
 * <pre>
 *  저널 일자 목록 검색 파라미터.
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
public class JrnlDaySearchParam
        extends BaseSearchParam {

    /** 년도 */
    private Integer yy;
    /** 월 */
    private Integer mnth;
    /** 기준일자 */
    private String stdrdDt;

    /** 컨텐츠 타입 */
    private String contentType;

    /** 정렬 (ASC/DESC) */
    @Builder.Default
    private String sort = "ASC";

    /** 태그 번호 */
    private Integer tagNo;
    /** 메타 번호 */
    private Integer metaNo;
}
