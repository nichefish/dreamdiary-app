package io.nicheblog.dreamdiary.domain.clsf.state.entity;

import io.nicheblog.dreamdiary.domain.clsf.state.model.StateToggleDto;
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
@Where(clause = "del_yn='N'")
public class StateEntity
        extends BaseCrudEntity {

    /** 상태 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "state_no")
    @Comment("상태 번호 (PK)")
    private Integer stateNo;

    /** 참조 글 번호 */
    @Column(name = "ref_post_no")
    @Comment("참조 글 번호")
    private Integer refPostNo;

    /** 참조 컨텐츠 타입 */
    @Column(name = "ref_content_type")
    @Comment("참조 컨텐츠 타입")
    private String refContentType;

    /** 상태 코드 */
    @Column(name = "state_cd")
    @Comment("상태 코드")
    private String stateCd;

    /**
     * 생성자
     * @param stateToggle 상태 파라미터
     * @return {@link StateEntity}
     */
    public static StateEntity of(final StateToggleDto stateToggle) {
        return StateEntity.builder()
                .refPostNo(stateToggle.getPostNo())
                .refContentType(stateToggle.getContentType().key)
                .stateCd(stateToggle.getStateCd().key)
                .build();
    }
}
