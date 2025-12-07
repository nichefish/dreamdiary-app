package io.nicheblog.dreamdiary.domain.jrnl.diary.entity;

import io.nicheblog.dreamdiary.domain.jrnl.entry.entity.JrnlEntrySmpEntity;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * JrnlDiarySmpEntity
 * <pre>
 *  저널 일기 Entity. (연관관계 간소화)
 *  Entity that contains each distinct diary.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "jrnl_diary")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE jrnl_diary SET del_yn = 'Y' WHERE post_no = ?")
public class JrnlDiarySmpEntity {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JRNL_DIARY;

    /** 저널 꿈 고유 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_no")
    @Comment("저널 일기 고유 번호")
    private Integer postNo;

    /** 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'JRNL_DIARY'")
    @Comment("컨텐츠 타입")
    private String contentType = CONTENT_TYPE.key;

    /* ----- */

    /** 저널 항목 번호  */
    @Column(name = "jrnl_entry_no")
    @Comment("저널 항목 번호")
    private Integer jrnlEntryNo;

    /** 저널 항목 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jrnl_entry_no", referencedColumnName = "post_no", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("저널 항목 정보")
    private JrnlEntrySmpEntity jrnlEntry;

    /** 순번 */
    @Column(name = "idx", columnDefinition = "INT DEFAULT 1")
    private Integer idx;

    /** 정리완료 여부 (Y/N) */
    @Builder.Default
    @Column(name = "resolved_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("정리완료 여부")
    private String resolvedYn = "N";
}
