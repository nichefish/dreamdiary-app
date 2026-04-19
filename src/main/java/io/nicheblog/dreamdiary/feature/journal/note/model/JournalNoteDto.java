package io.nicheblog.dreamdiary.feature.journal.note.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.attachable.history.model.cmpstn.HistoryCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.history.model.cmpstn.HistoryCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.related.model.RelatedContentDto;
import io.nicheblog.dreamdiary.feature.attachable.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.state.model.cmpstn.StateCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.feature.file.model.cmpstn.FileCmpstn;
import io.nicheblog.dreamdiary.feature.file.model.cmpstn.FileCmpstnModule;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalPeriodModule;
import io.nicheblog.dreamdiary.feature.journal.day.type.JournalDatePrecision;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class JournalNoteDto
        extends BaseAttachableDto
        implements Identifiable<Integer>, FileCmpstnModule, CommentCmpstnModule, TagCmpstnModule, StateCmpstnModule, HistoryCmpstnModule, JournalPeriodModule, Comparable<JournalNoteDto> {

    @Builder.Default
    private String contentType = ContentType.JOURNAL_NOTE.key;

    private String title;
    private String content;
    private String markdownContent;

    private Integer journalDayId;
    private Integer journalChapterId;
    private String stdrdDt;
    private JournalDatePrecision journalDatePrecision;
    private String journalDateWeekDay;
    private Integer yy;
    private Integer mnth;
    private Boolean isHolyday;
    private String holydayNm;
    private Integer sortOrder;

    @Builder.Default
    private Boolean isSortOrderChanged = false;

    @Builder.Default
    private Boolean isChapterChanged = false;

    private Integer prevJournalChapterId;

    @SneakyThrows
    @Override
    public int compareTo(final @NotNull JournalNoteDto other) {
        final Date thisDate = DateUtils.asDate(this.getStdrdDt());
        if (thisDate == null) return -1;

        final Date otherDate = DateUtils.asDate(other.getStdrdDt());
        return thisDate.compareTo(otherDate);
    }

    @Override
    public Integer getKey() {
        return this.id;
    }

    public FileCmpstn file;
    public CommentCmpstn comment;
    public TagCmpstn tag;
    public StateCmpstn state;
    public HistoryCmpstn history;
    private List<HistoryDto> historyList;

    @Builder.Default
    private List<RelatedContentDto> relatedContentList = List.of();
}
