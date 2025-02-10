package io.nicheblog.dreamdiary.extension.clsf.tag.entity;

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
 * TagPropertyEntity
 * <pre>
 *  태그 속성 Entity
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "tag_property")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE tag_property SET del_yn = 'Y' WHERE tag_property_no = ?")
public class TagPropertyEntity
        extends BaseCrudEntity {

    /** 태그 속성 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_property_no")
    @Comment("태그 속성 번호 (PK)")
    private Integer tagPropertyNo;

    /** 태그 번호 */
    @Column(name = "tag_no")
    @Comment("태그 번호")
    private Integer tagNo;

    /** 참조 컨텐츠 타입 */
    @Column(name = "content_type")
    @Comment("컨텐츠 타입")
    private String contentType;

    /* ----- */

    /** CSS 클래스 */
    @Column(name = "css_class")
    @Comment("CSS 클래스")
    private String cssClass;

    /** CSS 스타일 */
    @Column(name = "css_style")
    @Comment("CSS 스타일")
    private String cssStyle;
}