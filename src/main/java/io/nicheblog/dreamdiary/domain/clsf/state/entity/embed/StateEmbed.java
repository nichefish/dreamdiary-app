package io.nicheblog.dreamdiary.domain.clsf.state.entity.embed;

import io.nicheblog.dreamdiary.domain.clsf.state.StateCd;
import io.nicheblog.dreamdiary.domain.clsf.state.entity.StateEntity;
import lombok.*;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.annotations.*;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.List;

/**
 * StateEmbed
 * <pre>
 *  위임 :: 상태 관련 정보. (entity level)
 * </pre>
 *
 * @author nichefish
 * @see StateEmbedModule
 */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateEmbed
        implements Serializable {

    /**
     * 상태 목록
     */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "ref_post_no", referencedColumnName = "post_no", insertable = false, updatable = false)),
            @JoinColumnOrFormula(column = @JoinColumn(name = "ref_content_type", referencedColumnName = "content_type", insertable = false, updatable = false)),
    })
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 10)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("상태 목록")
    private List<StateEntity> list;

    /**
     * 상태 존재 여부 반환
     * @param stateCd 상태 코드
     * @return 상태 존재 여부
     */
    public boolean hasState(final StateCd stateCd) {
        if (stateCd == null || CollectionUtils.isEmpty(this.list)) return false;
        return this.list.stream()
            .anyMatch(s -> stateCd.key.equals(s.getStateCd()));
    }
}
