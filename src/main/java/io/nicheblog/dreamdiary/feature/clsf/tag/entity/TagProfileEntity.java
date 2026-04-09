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

/**
 * TagProfileEntity
 * <pre>
 *  태그 프로필(해석) Entity.
 *  태그와 컨텐츠 타입 조합에 대한 추가 설명과 시각 의미를 보관한다.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(
    name = "tag_profile",
    uniqueConstraints = @UniqueConstraint(name = "uk_tag_profile", columnNames = { "tag_no", "content_type", "regstr_id" } )
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE tag_profile SET del_yn = 'Y', content_type = CONCAT(content_type, '_del_', tag_profile_no) WHERE tag_profile_no = ?")
public class TagProfileEntity
        extends BaseAuditRegEntity {

    /** 태그 프로필 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_profile_no")
    @Comment("태그 프로필 번호 (PK)")
    private Integer tagProfileNo;

    /** 참조 태그 번호 */
    @Column(name = "tag_no", nullable = false)
    @Comment("참조 태그 번호")
    private Integer tagNo;

    /** 참조 컨텐츠 타입 */
    @Column(name = "content_type", length = 50, nullable = false)
    @Comment("참조 컨텐츠 타입")
    private String contentType;

    /** 프로필 본문 */
    @Column(name = "cn", columnDefinition = "LONGTEXT")
    @Comment("프로필 본문")
    private String cn;

    /** 시각 의미 */
    @Enumerated(EnumType.STRING)
    @Column(name = "text_class", length = 30, nullable = false)
    @Comment("시각 의미")
    private TextClass textClass;
}
