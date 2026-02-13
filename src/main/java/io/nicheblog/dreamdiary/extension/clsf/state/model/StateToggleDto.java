package io.nicheblog.dreamdiary.extension.clsf.state.model;

import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.extension.clsf.state.StateCd;
import lombok.*;

import javax.validation.constraints.Positive;

/**
 * MarkToggleDto
 *
 * @author nichefish
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateToggleDto {

    /** 글 번호 */
    @Positive
    private Integer postNo;
    /** 컨텐츠 타입 */
    private ContentType contentType;
    /** 표시 코드 */
    private StateCd stateCd;

    /** 캐시 파라미터 */
    private CacheContext cacheContext;
}
