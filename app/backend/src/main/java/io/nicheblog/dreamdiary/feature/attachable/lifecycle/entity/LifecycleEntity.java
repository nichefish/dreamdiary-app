package io.nicheblog.dreamdiary.feature.attachable.lifecycle.entity;

import io.nicheblog.dreamdiary.feature.attachable.lifecycle.model.LifecycleSetDto;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * 임의 컨텐츠에 붙는 라이프사이클 저장 엔티티.
 *
 * <p>유니크 기준은 {@code ref_content_type + ref_id}이다.
 * 라이프사이클은 toggle 이력이 아니라 현재 단계를 저장하며,
 * 기본 상태 {@code OPEN}은 row 부재로 표현한다.</p>
 */
@Entity
@Table(name = "lifecycle")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE lifecycle SET deleted_at = NOW() WHERE id = ?")
public class LifecycleEntity
        extends BaseCrudEntity {

    /** 라이프사이클 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("라이프사이클 ID")
    private Integer id;

    /** 참조 글 번호 */
    @Column(name = "ref_id")
    @Comment("참조 글 번호")
    private Integer refId;

    /** 참조 컨텐츠 타입 */
    @Column(name = "ref_content_type")
    @Comment("참조 컨텐츠 타입")
    private String refContentType;

    /** 라이프사이클 키 */
    @Column(name = "lifecycle_key")
    @Comment("라이프사이클 키")
    private String lifecycleKey;

    /**
     * 라이프사이클 설정 요청으로 신규 엔티티를 생성한다.
     *
     * @param lifecycleSet 라이프사이클 설정 요청
     * @return 저장할 라이프사이클 엔티티
     */
    public static LifecycleEntity of(final LifecycleSetDto lifecycleSet) {
        return LifecycleEntity.builder()
                .refId(lifecycleSet.getId())
                .refContentType(lifecycleSet.getContentType().key)
                .lifecycleKey(lifecycleSet.getLifecycleKey().key)
                .build();
    }
}
