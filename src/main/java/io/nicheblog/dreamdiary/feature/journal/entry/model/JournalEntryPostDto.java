package io.nicheblog.dreamdiary.feature.journal.entry.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.attachable.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.history.HistoryType;
import io.nicheblog.dreamdiary.feature.attachable.history.model.HistoryActionModule;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.feature.file.model.cmpstn.FileCmpstn;
import io.nicheblog.dreamdiary.feature.file.model.cmpstn.FileCmpstnModule;
import io.nicheblog.dreamdiary.feature.journal.interpretation.model.JournalInterpretationDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class JournalEntryPostDto extends BaseAttachableDto
        implements Identifiable<Integer>, FileCmpstnModule, CommentCmpstnModule, TagCmpstnModule, HistoryActionModule {

    private String title;
    private String content;
    private Integer journalChapterId;
    private Integer yy;
    private Integer mnth;
    private Integer sortOrder;
    private String contentType;
    private Integer journalDayId;
    private Integer prevJournalChapterId;
    private Integer fromHistoryId;
    private List<JournalInterpretationDto> journalInterpretationList;
    private String elseDreamerNm;

    @Builder.Default
    private Boolean isSortOrderChanged = false;

    @Builder.Default
    private Boolean isChapterChanged = false;

    @Builder.Default
    private String historyType = HistoryType.CHANGE.key;

    public FileCmpstn file;
    public CommentCmpstn comment;
    public TagCmpstn tag;

    /**
     * 식별 키를 반환한다.
     *
     * @return 엔트리 ID
     */
    @Override
    public Integer getKey() {
        return this.id;
    }
}
