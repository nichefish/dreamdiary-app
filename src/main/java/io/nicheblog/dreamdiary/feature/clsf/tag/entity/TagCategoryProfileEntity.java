package io.nicheblog.dreamdiary.feature.clsf.tag.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditRegEntity;
import io.nicheblog.dreamdiary.global.type.TextClass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
    name = "tag_category_profile",
    uniqueConstraints = @UniqueConstraint(name = "uk_tag_category_profile", columnNames = { "tag_category_id", "content_type", "regstr_id" })
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE tag_category_profile SET del_yn = 'Y', content_type = CONCAT(content_type, '_del_', id) WHERE id = ?")
public class TagCategoryProfileEntity extends BaseAuditRegEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("태그 카테고리 프로필 ID")
    private Integer id;

    @Column(name = "tag_category_id", nullable = false)
    @Comment("태그 카테고리 ID")
    private Integer tagCategoryId;

    @Column(name = "content_type", length = 50, nullable = false)
    @Comment("컨텐츠 타입")
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "text_class", length = 30, nullable = false)
    @Comment("시각 의미")
    private TextClass textClass;
}
