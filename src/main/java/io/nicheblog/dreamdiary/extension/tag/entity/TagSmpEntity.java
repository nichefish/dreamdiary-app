package io.nicheblog.dreamdiary.extension.tag.entity;

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
 * TagSmpEntity
 * <pre>
 *  태그 간소화 Entity. (순환참조 방지 위해 연관관계 제거)
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
public class TagSmpEntity
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
}
