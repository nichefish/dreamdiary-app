package io.nicheblog.dreamdiary.extension.clsf.viewer.model.cmpstn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nicheblog.dreamdiary.extension.clsf.viewer.entity.ViewerEntity;
import io.nicheblog.dreamdiary.extension.clsf.viewer.mapstruct.ViewerMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.viewer.model.ViewerDto;
import lombok.*;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ViewerCmpstn
 * <pre>
 *  컨텐츠 열람자 관련 정보 위임. (dto level)
 * </pre>
 *
 * @author nichefish
 * @see ViewerCmpstnModule
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewerCmpstn
        implements Serializable {

    /** 컨텐츠 열람자 목록 */
    private List<ViewerDto> list;

    /* ----- */

    /**
     * 열람자 :: List<Dto> -> List<Entity> 반환
     * @return 변환된 ViewerEntity 객체의 리스트.
     *         리스트가 비어있거나 null인 경우 null을 반환합니다.
     */
    @JsonIgnore
    public List<ViewerEntity> getEntityList() {
        if (CollectionUtils.isEmpty(this.list)) return null;
        return this.list.stream()
                .map(dto -> {
                    try {
                        return ViewerMapstruct.INSTANCE.toEntity(dto);
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }
}