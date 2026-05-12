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
import java.util.List;

/**
 * TagEntity
 * <pre>
 *  태그 Entity.
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
public class TagEntity extends BaseCrudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("태그 ID")
    private Integer id;

    @Transient
    private String ctgr;

    @Column(name = "tag_category_id")
    @Comment("태그 카테고리 ID")
    private Integer tagCategoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_category_id", referencedColumnName = "id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private TagCategoryEntity tagCategory;

    @Column(name = "name")
    @Comment("태그명")
    private String name;

    @OneToMany(mappedBy = "tag", fetch = FetchType.LAZY)
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 10)
    @NotFound(action = NotFoundAction.IGNORE)
    private List<TagContentEntity> tagContentList;

    public TagEntity(final String name) {
        this.name = name;
    }

    public TagEntity(final String name, final String ctgr) {
        this.name = name;
        this.ctgr = ctgr;
    }

    public String getCtgr() {
        if (this.tagCategory != null && this.tagCategory.getName() != null) {
            return this.tagCategory.getName();
        }
        return this.ctgr == null ? "" : this.ctgr;
    }

}
