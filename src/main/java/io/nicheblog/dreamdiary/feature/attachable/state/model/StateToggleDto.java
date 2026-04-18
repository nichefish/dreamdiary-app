package io.nicheblog.dreamdiary.feature.attachable.state.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.state.StateCd;
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
    private Integer id;
    /** 컨텐츠 타입 */
    private ContentType contentType;
    /** 표시 코드 */
    private StateCd stateCode;

    /** 캐시 파라미터 */
    private CacheContext cacheContext;

    public Integer getId() {
        return this.id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }
}
