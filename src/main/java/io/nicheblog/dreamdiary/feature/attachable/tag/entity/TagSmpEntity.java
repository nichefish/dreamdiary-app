package io.nicheblog.dreamdiary.feature.attachable.tag.entity;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * TagSmpEntity
 * <pre>
 *  태그 간소화 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "tag")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE tag SET deleted_at = NOW() WHERE id = ?")
public class TagSmpEntity
        extends BaseCrudEntity {

    /** 태그 ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("태그 ID")
    private Integer id;

    /** 태그 카테고리 */
    @Transient
    private String ctgr;

    /** 태그 카테고리 ID */
    @Column(name = "tag_category_id")
    @Comment("태그 카테고리 ID")
    private Integer tagCategoryId;

    /** 태그 카테고리 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_category_id", referencedColumnName = "id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private TagCategoryEntity tagCategory;

    /** 태그 이름 */
    @Column(name = "tag_nm")
    @Comment("태그 이름")
    private String tagNm;

    public String getCtgr() {
        if (this.tagCategory != null && this.tagCategory.getCtgrNm() != null) {
            return this.tagCategory.getCtgrNm();
        }
        return this.ctgr == null ? "" : this.ctgr;
    }

}
