package io.nicheblog.dreamdiary.feature.attachable.prefix.entity.embed;

import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixContentEntity;
import lombok.*;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.annotations.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * PrefixEmbed
 * <pre>
 *  위임 :: 말머리 선택 정보. (entity level)
 *  콘텐츠 엔티티는 prefix FK를 직접 들지 않고, (ref_id, ref_content_type)=BaseAttachableKey로
 *  조인되는 prefix_content 연결을 이 임베드로 조립한다. 콘텐츠당 0..1이며, soft-delete 행의
 *  유니크 키 점유를 피하기 위해 DB UNIQUE는 두지 않고 선택 서비스의 단일 upsert 경로가
 *  활성 연결 단일성을 보장한다. (meta/state 임베드와 동일 방식.)
 * </pre>
 *
 * @author nichefish
 * @see PrefixEmbedModule
 */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrefixEmbed
        implements Serializable {

    /**
     * 말머리 연결 목록 (0..1).
     * 콘텐츠당 최대 1건이지만, 검증된 attachable 조인 방식(@OneToMany)을 그대로 사용하고
     * 단건 접근은 {@link #getSelected()}로 제공한다.
     */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "ref_id", referencedColumnName = "id", insertable = false, updatable = false)),
            @JoinColumnOrFormula(column = @JoinColumn(name = "ref_content_type", referencedColumnName = "content_type", insertable = false, updatable = false)),
    })
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 50)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("말머리 연결")
    private List<PrefixContentEntity> list;

    /**
     * 선택된 말머리 연결(0..1)을 반환한다. 없으면 {@code null}.
     * @return 말머리 연결 또는 null
     */
    public PrefixContentEntity getSelected() {
        if (CollectionUtils.isEmpty(this.list)) return null;
        return this.list.get(0);
    }
}
