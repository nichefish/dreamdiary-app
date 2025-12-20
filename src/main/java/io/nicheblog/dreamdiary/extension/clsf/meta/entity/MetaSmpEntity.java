package io.nicheblog.dreamdiary.extension.clsf.meta.entity;

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
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE meta SET del_yn = 'Y' WHERE meta_no = ?")
public class MetaSmpEntity
        extends BaseCrudEntity {

    /** 메타 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meta_no")
    @Comment("메타 번호 (PK)")
    private Integer metaNo;

    /** 메타 카테고리 */
    @Column(name = "ctgr")
    @Comment("메타 카테고리")
    private String ctgr;

    /** 메타 */
    @Column(name = "meta_nm")
    @Comment("메타")
    private String metaNm;

    /** 메타 이름 */
    @Column(name = "label")
    @Comment("메타 라벨")
    private String label;
}
