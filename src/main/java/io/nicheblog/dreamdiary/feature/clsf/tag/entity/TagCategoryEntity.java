package io.nicheblog.dreamdiary.feature.clsf.tag.entity;

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
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * TagCategoryEntity
 * <pre>
 *  태그 카테고리 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "tag_category")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE tag_category SET del_yn = 'Y' WHERE id = ?")
public class TagCategoryEntity
        extends BaseCrudEntity {

    /** 태그 카테고리 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("태그 카테고리 ID")
    private Integer id;

    /** 태그 카테고리 이름 */
    @Column(name = "ctgr_nm")
    @Comment("태그 카테고리 이름")
    private String ctgrNm;

    public TagCategoryEntity(final String ctgrNm) {
        this.ctgrNm = ctgrNm;
    }
}
