package io.nicheblog.dreamdiary.domain.jrnl.dream.entity;

import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDaySmpEntity;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;

/**
 * JrnlDreamSmpEntity
 * <pre>
 *  저널 꿈 Entity. (연관관계 간소화)
 *  Entity that contains each distinct dream.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "jrnl_dream")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE jrnl_dream SET del_yn = 'Y' WHERE post_no = ?")
public class JrnlDreamSmpEntity {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JRNL_DREAM;

    /** 저널 꿈 고유 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_no")
    @Comment("저널 꿈 고유 번호")
    private Integer postNo;

    /** 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'JRNL_DREAM'")
    @Comment("컨텐츠 타입")
    private String contentType = CONTENT_TYPE.key;

    /* ----- */

    /** 저널 일자 번호  */
    @Column(name = "jrnl_day_no")
    @Comment("저널 일자 번호")
    private Integer jrnlDayNo;

    /** 저널 일자 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jrnl_day_no", referencedColumnName = "post_no", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("저널 일자 정보")
    private JrnlDaySmpEntity jrnlDay;
}
