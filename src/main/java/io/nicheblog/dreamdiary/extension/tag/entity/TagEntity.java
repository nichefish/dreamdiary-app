package io.nicheblog.dreamdiary.extension.tag.entity;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
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
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE tag SET del_yn = 'Y' WHERE tag_no = ?")
public class TagEntity
        extends BaseCrudEntity {

    /** 태그 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_no")
    @Comment("태그 번호 (PK)")
    private Integer tagNo;

    /** 태그 카테고리 */
    @Column(name = "ctgr")
    @Comment("태그 카테고리")
    private String ctgr;

    /** 태그 */
    @Column(name = "tag_nm")
    @Comment("태그")
    private String tagNm;

    /** 컨텐츠 태그 */
    @OneToMany(mappedBy = "tag", fetch = FetchType.LAZY)
    @Fetch(FetchMode.SELECT)
    @NotFound(action = NotFoundAction.IGNORE)
    private List<ContentTagEntity> contentTagList;

    /** 태그 속성 */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "tag_no", insertable = false, updatable = false)
    @Fetch(FetchMode.SELECT)
    @NotFound(action = NotFoundAction.IGNORE)
    private List<TagPropertyEntity> propertyList;

    /* ----- */

    /**
     * 생성자.
     *
     * @param tagNm - 생성할 태그의 이름
     */
    public TagEntity(final String tagNm) {
        this.tagNm = tagNm;
    }

    /**
     * 생성자.
     *
     * @param tagNm - 생성할 태그의 이름
     * @param ctgr - 생성할 태그의 카테고리
     */
    public TagEntity(final String tagNm, final String ctgr) {
        this.tagNm = tagNm;
        this.ctgr = ctgr;
    }
}
