package io.nicheblog.dreamdiary.feature.journal.sbjct.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * JournalSbjctSmpEntity
 *
 * @author nichefish
 */
@Entity
@Table(name = "journal_sbjct")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE journal_sbjct SET deleted_at = NOW() WHERE id = ?")
public class JournalSbjctSmpEntity
        extends BaseAttachableEntity {

    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JOURNAL_SBJCT;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("저널 주제 고유 번호")
    private Integer id;

    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'JOURNAL_SBJCT'")
    @Comment("콘텐츠 타입")
    private String contentType = CONTENT_TYPE.key;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "category_code", length = 50)
    @Comment("글 분류 코드")
    private String categoryCode;
}
