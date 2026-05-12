package io.nicheblog.dreamdiary.feature.journal.thread.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.feature.file.model.cmpstn.FileCmpstn;
import io.nicheblog.dreamdiary.feature.file.model.cmpstn.FileCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Size;

/**
 * JournalThreadDto
 * 저널 스레드(JOURNAL_THREAD) 첨부 콘텐츠의 뷰·등록용 DTO.
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JournalThreadDto
        extends BaseAttachableDto
        implements Identifiable<Integer>, FileCmpstnModule, CommentCmpstnModule, TagCmpstnModule {

    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JOURNAL_THREAD;

    @Builder.Default
    private String contentType = CONTENT_TYPE.key;

    private String title;

    private String content;

    private String markdownContent;

    @Size(max = 50)
    private String categoryCode;

    @Size(max = 50)
    private String categoryName;

    @Getter
    @Setter
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class DTL extends JournalThreadDto {
        //
    }

    @Getter
    @Setter
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class LIST extends JournalThreadDto {
        //
    }

    @Override
    public Integer getKey() {
        return this.id;
    }

    public FileCmpstn file;
    public CommentCmpstn comment;
    public TagCmpstn tag;
}
