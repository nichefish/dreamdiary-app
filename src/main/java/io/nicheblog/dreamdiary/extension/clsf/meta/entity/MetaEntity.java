package io.nicheblog.dreamdiary.extension.clsf.meta.entity;

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
 * MetaEntity
 * <pre>
 *  메타 Entity.
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
public class MetaEntity
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

    /** 메타 이름 */
    @Column(name = "meta_nm")
    @Comment("메타 이름")
    private String metaNm;

    /** 메타 라벨 */
    @Column(name = "label")
    @Comment("메타 라벨")
    private String label;

    /** 메타-컨텐츠 */
    @OneToMany(mappedBy = "meta", fetch = FetchType.LAZY)
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 10)
    @NotFound(action = NotFoundAction.IGNORE)
    private List<MetaContentEntity> metaContentList;

    /** 값 (전달용) */
    @Transient
    private String value;
    
    /** 단위 (전달용) */
    @Transient
    private String unit;
}
