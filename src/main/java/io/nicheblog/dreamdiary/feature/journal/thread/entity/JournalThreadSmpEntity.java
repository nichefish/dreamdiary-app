package io.nicheblog.dreamdiary.feature.journal.thread.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * JournalThreadSmpEntity
 * 저널 스레드(JOURNAL_THREAD) 목록·참조용 간소화 엔티티.
 *
 * @author nichefish
 */
@Entity
@Table(name = "journal_thread")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE journal_thread SET deleted_at = NOW() WHERE id = ?")
public class JournalThreadSmpEntity
        extends BaseAttachableEntity {

    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JOURNAL_THREAD;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("저널 스레드 고유 번호")
    private Integer id;

    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'JOURNAL_THREAD'")
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
