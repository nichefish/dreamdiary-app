package io.nicheblog.dreamdiary.feature.journal.entry.entity;

import io.nicheblog.dreamdiary.feature.attachable.tag.entity.TagCategoryEntity;
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

@Entity
@Table(name = "tag")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE tag SET deleted_at = NOW() WHERE id = ?")
public class JournalEntryTagEntity
        extends BaseCrudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("tag id")
    private Integer id;

    @Transient
    private String ctgr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_category_id", referencedColumnName = "id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private TagCategoryEntity tagCategory;

    @Column(name = "tag_nm")
    @Comment("tag name")
    private String tagNm;

    @OneToMany(mappedBy = "tag", fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 10)
    @NotFound(action = NotFoundAction.IGNORE)
    private List<JournalEntryTagContentEntity> journalEntryTagList;

    /**
     * 태그 카테고리명을 반환한다.
     *
     * @return 카테고리명(없으면 빈 문자열)
     */
    public String getCtgr() {
        if (this.tagCategory != null && this.tagCategory.getCtgrNm() != null) {
            return this.tagCategory.getCtgrNm();
        }
        return this.ctgr == null ? "" : this.ctgr;
    }
}
