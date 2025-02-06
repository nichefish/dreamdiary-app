package io.nicheblog.dreamdiary.domain.jrnl.sbjct.entity;

import io.nicheblog.dreamdiary.extension.ContentType;
import io.nicheblog.dreamdiary.global.intrfc.entity.BasePostEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * JrnlSbjctEntity
 * <pre>
 *  저널 주제 Entity.
 *  Entity that contains distinctive subject.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "jrnl_sbjct")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE jrnl_sbjct SET del_yn = 'Y' WHERE post_no = ?")
public class JrnlSbjctSmpEntity
        extends BasePostEntity {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JRNL_SBJCT;

    /** 저널 주제 고유 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_no")
    @Comment("저널 주제 고유 번호")
    private Integer postNo;

    /** 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'JRNL_SBJCT'")
    @Comment("컨텐츠 타입")
    private String contentType = CONTENT_TYPE.key;
}
