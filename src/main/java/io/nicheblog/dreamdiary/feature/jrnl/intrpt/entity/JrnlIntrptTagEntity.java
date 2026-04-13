package io.nicheblog.dreamdiary.feature.jrnl.intrpt.entity;

import io.nicheblog.dreamdiary.feature.clsf.tag.entity.TagCategoryEntity;
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
 * JrnlIntrptTagEntity
 * <pre>
 *  저널 해석 태그 Entity.
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
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE tag SET del_yn = 'Y' WHERE id = ?")
public class JrnlIntrptTagEntity
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_category_id", referencedColumnName = "id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private TagCategoryEntity tagCategory;

    /** 태그 이름 */
    @Column(name = "tag_nm")
    @Comment("태그 이름")
    private String tagNm;

    /** 태그-컨텐츠 목록 */
    @OneToMany(mappedBy = "tag", fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 10)
    @NotFound(action = NotFoundAction.IGNORE)
    private List<JrnlIntrptTagContentEntity> jrnlIntrptTagList;

    public String getCtgr() {
        if (this.tagCategory != null && this.tagCategory.getCtgrNm() != null) {
            return this.tagCategory.getCtgrNm();
        }
        return this.ctgr == null ? "" : this.ctgr;
    }

}
