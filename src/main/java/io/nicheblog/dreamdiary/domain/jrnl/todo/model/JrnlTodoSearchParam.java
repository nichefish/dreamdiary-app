package io.nicheblog.dreamdiary.domain.jrnl.todo.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * JrnlTodoSearchParam
 * <pre>
 *  저널 할일 목록 검색 파라미터.
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
public class JrnlTodoSearchParam
        extends BaseSearchParam {

    /** 년도 */
    private Integer yy;

    /** 월 */
    private Integer mnth;

    /** 컨텐츠 타입 */
    private String contentType;

    /** 태그 번호 */
    private Integer tagNo;

    /** 중요 여부 **/
    private String imprtcYn;
}
