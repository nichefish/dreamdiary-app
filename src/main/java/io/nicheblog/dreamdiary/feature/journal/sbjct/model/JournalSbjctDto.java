package io.nicheblog.dreamdiary.feature.journal.sbjct.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.feature.file.model.cmpstn.FileCmpstn;
import io.nicheblog.dreamdiary.feature.file.model.cmpstn.FileCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Size;

/**
 * JournalSbjctDto
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JournalSbjctDto
        extends BaseAttachableDto
        implements Identifiable<Integer>, FileCmpstnModule, CommentCmpstnModule, TagCmpstnModule {

    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JOURNAL_SBJCT;

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
    public static class DTL extends JournalSbjctDto {
        //
    }

    @Getter
    @Setter
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class LIST extends JournalSbjctDto {
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
