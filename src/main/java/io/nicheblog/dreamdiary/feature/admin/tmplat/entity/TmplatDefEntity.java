package io.nicheblog.dreamdiary.feature.admin.tmplat.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * TmplatDefEntity
 * <pre>
 *  템플릿(사전입력폼) 정의 정보 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "tmplat_def")
@DynamicInsert      // null인 값은 (null로 insert하는 대신) insert에서 제외
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE tmplat_def SET deleted_at = NOW() WHERE id = ?")
public class TmplatDefEntity
        extends BaseAuditEntity {

    /** 템플릿 정의 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("템플릿 정의 ID")
    private Integer id;

    /** 템플릿 정의 코드 */
    @Column(name = "tmplat_def_cd", length = 1000)
    @Comment("템플릿 정의 코드")
    private String tmplatDefCd;

    /** 제목 */
    @Column(name = "title", length = 1000)
    @Comment("제목")
    private String title;
}
