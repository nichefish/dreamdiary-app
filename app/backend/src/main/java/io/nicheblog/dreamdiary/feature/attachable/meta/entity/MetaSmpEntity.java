package io.nicheblog.dreamdiary.feature.attachable.meta.entity;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * MetaSmpEntity
 * <pre>
 *  메타 간소화 Entity. (순환참조 방지 위해 연관관계 제거)
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "meta")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE meta SET deleted_at = NOW() WHERE id = ?")
public class MetaSmpEntity
        extends BaseCrudEntity {

    /** 메타 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("메타 ID")
    private Integer id;

    /** 메타 카테고리 */
    @Column(name = "ctgr")
    @Comment("메타 카테고리")
    private String ctgr;

    /** 메타 */
    @Column(name = "name")
    @Comment("메타")
    private String name;

}
