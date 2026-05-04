package io.nicheblog.dreamdiary.feature.attachable.lifecycle.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.AttachableCacheContext;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.LifecycleKey;
import lombok.*;

import javax.validation.constraints.Positive;

/**
 * 부착 가능 컨텐츠 하나의 라이프사이클 값을 설정하는 요청 DTO.
 *
 * <p>클라이언트가 변경 요청과 함께 캐시 컨텍스트를 보내면, 서버는 월간/주간 목록 전체를
 * 다시 만들지 않고 라이프사이클 보조 맵만 부분 갱신할 수 있다.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LifecycleSetDto {

    @Positive
    private Integer id;

    private ContentType contentType;

    private LifecycleKey lifecycleKey;

    private AttachableCacheContext cacheContext;
}
