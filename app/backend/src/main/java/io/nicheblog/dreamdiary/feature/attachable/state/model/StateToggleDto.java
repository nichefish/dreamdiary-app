package io.nicheblog.dreamdiary.feature.attachable.state.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.state.StateKey;
import lombok.*;

import javax.validation.constraints.Positive;

/**
 * StateToggleDto
 * 첨부 콘텐츠 상태 토글 REST 요청 바디(id, 타입, 상태 키, 캐시 맥락).
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
    /** 상태 키 */
    private StateKey stateKey;

    /** 캐시 파라미터 */
    private CacheContext cacheContext;


}
