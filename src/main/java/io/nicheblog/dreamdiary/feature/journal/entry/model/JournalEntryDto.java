package io.nicheblog.dreamdiary.feature.journal.entry.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
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
import io.nicheblog.dreamdiary.feature.journal.interpretation.model.JournalInterpretationDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.global.validator.state.UpdateState;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;

import javax.validation.constraints.Pattern;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class JournalEntryDto extends BaseAttachableDto
        implements Identifiable<Integer>, Comparable<JournalEntryDto>,
        FileCmpstnModule, CommentCmpstnModule, TagCmpstnModule, StateCmpstnModule,
        HistoryCmpstnModule, JournalPeriodModule {

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

    private String contentType;

    @Builder.Default
    private Boolean isSortOrderChanged = false;

    public FileCmpstn file;
    public CommentCmpstn comment;
    public TagCmpstn tag;
    public StateCmpstn state;
    public HistoryCmpstn history;
    private List<HistoryDto> historyList;

    @Builder.Default
    private List<RelatedContentDto> relatedContentList = List.of();

    private List<JournalInterpretationDto> journalInterpretationList;

    @Builder.Default
    @Pattern(regexp = "^[YN]$", groups = UpdateState.class)
    private String elseDreamYn = "N";

    private String elseDreamerNm;

    @Builder.Default
    private Boolean isChapterChanged = false;

    private Integer prevJournalChapterId;

    /**
     * 기준일 문자열을 기준으로 정렬 순서를 비교한다.
     *
     * @param other 비교 대상 DTO
     * @return 비교 결과
     */
    @SneakyThrows
    @Override
    public int compareTo(final @NotNull JournalEntryDto other) {
        return compareByStdrdDt(other.getStdrdDt());
    }

    /**
     * 식별 키를 반환한다.
     *
     * @return 엔트리 ID
     */
    @Override
    public Integer getKey() {
        return this.id;
    }

    @SneakyThrows
    private int compareByStdrdDt(final String otherStdrdDt) {
        final Date thisDate = DateUtils.asDate(this.getStdrdDt());
        if (thisDate == null) return -1;

        final Date otherDate = DateUtils.asDate(otherStdrdDt);
        return thisDate.compareTo(otherDate);
    }
}
