package io.nicheblog.dreamdiary.extension.log.sys.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * LogSysSearchParam
 * <pre>
 *  시스템 로그 목록 검색 파라미터.
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
public class LogSysSearchParam
        extends BaseSearchParam {

    /** 글분류 코드 */
    private String ctgrCd;

    /** 제목 */
    private String title;
}
