package io.nicheblog.dreamdiary.feature.attachable.state.entity;

import io.nicheblog.dreamdiary.feature.attachable.state.model.StateToggleDto;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * StateEntity
 * <pre>
 *  상태 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "state")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class StateEntity
        extends BaseCrudEntity {

    /** 상태 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("상태 ID")
    private Integer id;

    /** 참조 글 번호 */
    @Column(name = "ref_id")
    @Comment("참조 글 번호")
    private Integer refId;

    /** 참조 컨텐츠 타입 */
    @Column(name = "ref_content_type")
    @Comment("참조 컨텐츠 타입")
    private String refContentType;

    /** 상태 키 */
    @Column(name = "state_key")
    @Comment("상태 키")
    private String stateKey;

    /**
     * 생성자
     * @param stateToggle 상태 파라미터
     * @return {@link StateEntity}
     */
    public static StateEntity of(final StateToggleDto stateToggle) {
        return StateEntity.builder()
                .refId(stateToggle.getId())
                .refContentType(stateToggle.getContentType().key)
                .stateKey(stateToggle.getStateKey().key)
                .build();
    }
}
