package io.nicheblog.dreamdiary.feature.attachable.state.entity.embed;

import io.nicheblog.dreamdiary.feature.attachable.state.StateKey;
import io.nicheblog.dreamdiary.feature.attachable.state.entity.StateEntity;
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
            @JoinColumnOrFormula(column = @JoinColumn(name = "ref_id", referencedColumnName = "id", insertable = false, updatable = false)),
            @JoinColumnOrFormula(column = @JoinColumn(name = "ref_content_type", referencedColumnName = "content_type", insertable = false, updatable = false)),
    })
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 50)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("상태 목록")
    private List<StateEntity> list;

    /**
     * 상태 존재 여부 반환
     * @param stateKey 상태 키
     * @return 상태 존재 여부
     */
    public boolean hasState(final StateKey stateKey) {
        if (stateKey == null || CollectionUtils.isEmpty(this.list)) return false;
        return this.list.stream()
            .anyMatch(s -> stateKey.key.equals(s.getStateKey()));
    }
}
