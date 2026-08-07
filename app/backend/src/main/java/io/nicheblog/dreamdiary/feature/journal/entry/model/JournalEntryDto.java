package io.nicheblog.dreamdiary.feature.journal.entry.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.attachable.history.model.cmpstn.HistoryCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.history.model.cmpstn.HistoryCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.model.cmpstn.LifecycleCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.model.cmpstn.LifecycleCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import io.nicheblog.dreamdiary.feature.attachable.related.model.RelatedContentDto;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadEntryDto;
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
        LifecycleCmpstnModule, HistoryCmpstnModule, JournalPeriodModule {

    private String title;
    private String content;
    private String markdownContent;

    private Integer journalDayId;
    /**
     * 소속 일자의 일기 축 완결 (Y/N). journal_day 투영. 검색·상세 등 일자 provide 없는 화면의 UI 잠금 SSOT.
     */
    private String diaryResolvedYn;
    /**
     * 소속 일자의 꿈 축 완결 (Y/N). journal_day 투영.
     */
    private String dreamResolvedYn;
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
    /** Reflection target 엔트리 ID. 독립이면 null. */
    private Integer refId;
    /** Reflection target 콘텐츠 타입. 독립이면 null. */
    private ContentType refContentType;
    /** 엔트리가 선택한 개인 말머리(0..1) */
    private PrefixDto prefix;
    /** 등록·수정 payload에서 선택한 개인 말머리 ID */
    private Integer prefixId;
    /** 소속 챕터 유형으로 해석한 개인 Prefix 목록 content_type */
    private String prefixContentType;

    @Builder.Default
    private Boolean isSortOrderChanged = false;

    public FileCmpstn file;
    public CommentCmpstn comment;
    public TagCmpstn tag;
    public StateCmpstn state;
    public LifecycleCmpstn lifecycle;
    public HistoryCmpstn history;
    private List<HistoryDto> historyList;

    @Builder.Default
    private List<RelatedContentDto> relatedContentList = List.of();

    /**
     * 이 엔트리가 속한 흐름(저널 스레드) 목록.
     * <p>
     * 한 엔트리가 여러 스레드에 속할 수 있어 목록이다. 소속이 없으면 빈 목록이다.
     * FLOW 를 대체·수렴한 축이다(FLOW 관계·요약은 제거됨).
     * 목록 화면 N+1 을 피하려고 엔트리 목록 단위로 일괄 주입한다.
     */
    @Builder.Default
    private List<JournalThreadEntryDto> threadList = List.of();

    /**
     * 뷰 합성(relatedThreadIds) 응답에서 이 엔트리의 출처 스레드 ID.
     * <p>
     * base 스레드 소속 엔트리는 {@code null}. 연관 스레드에서 빌려온 엔트리는 연관 스레드 ID.
     * base·연관 양쪽 소속(중복)은 base 멤버로 취급해 {@code null}.
     * 설계 정본: docs/migration/journal/thread-relation.md §4
     * </p>
     */
    private Integer sourceThreadId;

    /** target 이 이 엔트리인 Reflection 목록 (역참조 교차뷰). reflection 은 자기 chapter 의 1급 엔트리이기도 하다. */
    private List<JournalEntryDto> reflectionList;

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
