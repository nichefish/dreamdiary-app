package io.nicheblog.dreamdiary.feature.journal.sbjct.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.comment.entity.embed.CommentEmbed;
import io.nicheblog.dreamdiary.feature.attachable.comment.entity.embed.CommentEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.embed.TagEmbedModule;
import io.nicheblog.dreamdiary.feature.file.entity.embed.FileEmbed;
import io.nicheblog.dreamdiary.feature.file.entity.embed.FileEmbedModule;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * JournalSbjctEntity
 * 주제(JOURNAL_SBJCT) 영속 엔티티. 파일·댓글·태그 임베드를 포함한다.
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
public class JournalSbjctEntity
        extends BaseAttachableEntity
        implements FileEmbedModule, CommentEmbedModule, TagEmbedModule {

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

    @Column(name = "category_code", length = 50)
    @Comment("저널 주제 글분류 코드 정보")
    private String categoryCode;

    @Transient
    private String categoryName;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Embedded
    public FileEmbed file;

    @Embedded
    public CommentEmbed comment;

    @Embedded
    public TagEmbed tag;
}
