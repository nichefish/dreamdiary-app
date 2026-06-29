package io.nicheblog.dreamdiary.feature.attachable.lifecycle.model;

import io.nicheblog.dreamdiary.feature.attachable.lifecycle.LifecycleKey;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * 저장된 라이프사이클 row 하나를 표현하는 API/화면 DTO.
 *
 * <p>현재 라이프사이클은 단일 키와 표시 문구만 노출하므로 DTO도 작게 유지한다.</p>
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LifecycleDto
        extends BaseCrudDto
        implements Identifiable<Integer> {

    @Positive
    private Integer id;

    @Positive
    private Integer refId;

    @Size(max = 50)
    private String refContentType;

    @Size(max = 50)
    private String lifecycleKey;

    private String lifecycleDesc;

    @Override
    public Integer getKey() {
        return this.id;
    }

    /**
     * 라이프사이클 열거형으로 간단한 DTO를 생성한다.
     *
     * @param lifecycleKey 라이프사이클 열거형 값
     * @return 키와 설명을 담은 DTO
     */
    public static LifecycleDto of(final LifecycleKey lifecycleKey) {
        if (lifecycleKey == null) return null;
        return LifecycleDto.builder()
                .lifecycleKey(lifecycleKey.key)
                .lifecycleDesc(lifecycleKey.getLabel())
                .build();
    }
}
