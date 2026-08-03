package io.nicheblog.dreamdiary.feature.journal.thread.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.comment.entity.embed.CommentEmbed;
import io.nicheblog.dreamdiary.feature.attachable.comment.entity.embed.CommentEmbedModule;
import io.nicheblog.dreamdiary.feature.file.entity.embed.FileEmbed;
import io.nicheblog.dreamdiary.feature.file.entity.embed.FileEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.embed.PrefixEmbed;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.embed.PrefixEmbedModule;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * JournalThreadEntity
 * 저널 스레드(JOURNAL_THREAD) 영속 엔티티. 파일·댓글·태그 임베드를 포함한다.
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
public class JournalThreadEntity
        extends BaseAttachableEntity
        implements FileEmbedModule, CommentEmbedModule, PrefixEmbedModule {

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

    /**
     * 제목 앞에 표시할 사용자 소유 말머리(0..1).
     * 콘텐츠는 prefix FK를 직접 들지 않고, (ref_id, ref_content_type)로 조인되는 prefix_content
     * 연결을 PrefixEmbed로 조립한다. (변경 전: journal_thread.prefix_id ad-hoc FK를 직접 보유했다.)
     */
    @Embedded
    public PrefixEmbed prefix;

    @Embedded
    public FileEmbed file;

    @Embedded
    public CommentEmbed comment;

}
