package io.nicheblog.dreamdiary.domain.jrnl.intrpt.entity;

import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamSmpEntity;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * JrnlIntrptSmpEntity
 * <pre>
 *  저널 해석 Entity. (연관관계 간소화)
 *  Entity that contains each distinct dream.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "jrnl_intrpt")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE jrnl_intrpt SET del_yn = 'Y' WHERE post_no = ?")
public class JrnlIntrptSmpEntity {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JRNL_ENTRY;

    /** 저널 항목 고유 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_no")
    @Comment("저널 항목 고유 번호")
    private Integer postNo;

    /** 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'JRNL_ENTRY'")
    @Comment("컨텐츠 타입")
    private String contentType = CONTENT_TYPE.key;

    /* ----- */

    /** 순번 */
    @Column(name = "idx", columnDefinition = "INT DEFAULT 1")
    private Integer idx;

    /** 저널 꿈 번호  */
    @Column(name = "jrnl_dream_no")
    @Comment("저널 꿈 번호")
    private Integer jrnlDreamNo;

    /** 저널 꿈 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jrnl_dream_no", referencedColumnName = "post_no", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("저널 꿈 정보")
    private JrnlDreamSmpEntity jrnlDream;
}
