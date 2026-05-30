package io.nicheblog.dreamdiary.feature.journal.entry.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.global.intrfc.model.fullcalendar.BaseCalDto;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JournalEntryCalDto
        extends BaseCalDto {

    @Builder.Default
    private String contentType = ContentType.JOURNAL_DIARY.key;

    private Integer journalDayId;

    @Builder.Default
    private String imprtcYn = "N";

    private String content;

    private String markdownContent;

    public TagCmpstn tag;

    public String getTitle() {
        final String hashTagStr = this.tag != null ? this.tag.getHashTagStr() : null;
        return StringUtils.defaultString(hashTagStr, getFallbackTitle());
    }

    public String getTextColor() {
        if (isDream()) return "rgba(95,0,130,0.8)";
        return "#8e8e8e";
    }

    public String getIcon() {
        if (isDream()) return "<i class=\"bi bi-moon-stars text-dream\"></i>";
        return "<i class=\"bi bi-book\"></i>";
    }

    public String getClassName() {
        if (isDream()) return "bg-transparent border-transparent text-dream text-truncate";
        return "bg-transparent border-transparent text-truncate";
    }

    public String getGroupId() {
        return this.contentType;
    }

    public int getTypePriority() {
        if (ContentType.JOURNAL_DIARY.key.equals(this.contentType)) return 3;
        if (ContentType.JOURNAL_DREAM.key.equals(this.contentType)) return 4;
        return 2;
    }

    private String getFallbackTitle() {
        if (ContentType.JOURNAL_DREAM.key.equals(this.contentType)) return "(dream)";
        return "(diary)";
    }

    private boolean isDream() {
        return ContentType.JOURNAL_DREAM.key.equals(this.contentType);
    }
}
