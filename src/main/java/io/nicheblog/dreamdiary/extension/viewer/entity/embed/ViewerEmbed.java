package io.nicheblog.dreamdiary.extension.viewer.entity.embed;

import io.nicheblog.dreamdiary.extension.viewer.entity.ViewerEntity;
import io.nicheblog.dreamdiary.extension.viewer.mapstruct.ViewerMapstruct;
import io.nicheblog.dreamdiary.extension.viewer.model.ViewerDto;
import lombok.*;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.annotations.*;

import javax.persistence.OrderBy;
import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ViewerEmbed
 * <pre>
 *  컨텐츠 열람자 관련 정보 위임. (entity level)
 * </pre>
 *
 * @author nichefish
 */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewerEmbed
        implements Serializable {

    /** 컨텐츠 열람자 목록 */
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "ref_post_no", referencedColumnName = "post_no", insertable = false, updatable = false)),
            @JoinColumnOrFormula(column = @JoinColumn(name = "ref_content_type", referencedColumnName = "content_type", insertable = false, updatable = false))
    })
    @Fetch(FetchMode.SELECT)
    @OrderBy("regDt DESC")
    @NotFound(action = NotFoundAction.IGNORE)
    private List<ViewerEntity> list;

    /* ----- */

    /**
     * 열람자 :: List<Entity> -> List<Dto> 반환
     * @return 변환된 ViewerDto 객체의 리스트.
     *         리스트가 비어있거나 null인 경우 null을 반환합니다.
     */
    public List<ViewerDto> getDtoList() {
        if (CollectionUtils.isEmpty(this.list)) return null;
        return this.list.stream()
                .map(entity -> {
                    try {
                        return ViewerMapstruct.INSTANCE.toDto(entity);
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }
}