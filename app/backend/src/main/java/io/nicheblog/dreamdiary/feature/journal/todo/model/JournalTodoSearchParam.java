package io.nicheblog.dreamdiary.feature.journal.todo.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * JournalTodoSearchParam
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
public class JournalTodoSearchParam
        extends BaseSearchParam {

    /** 년도 */
    private Integer yy;

    /** 월 */
    private Integer mnth;

    /** 컨텐츠 타입 */
    private String contentType;

    /** 태그 ID */
    private Integer tagId;
}
