package io.nicheblog.dreamdiary.feature.journal.chapter.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.state.model.cmpstn.StateCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalPeriodModule;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

/**
 * JournalChapterDto
 * <pre>
 *  저널 챕터 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class JournalChapterDto
        extends BaseAttachableDto
        implements Identifiable<Integer>, StateCmpstnModule, TagCmpstnModule, JournalPeriodModule, Comparable<JournalChapterDto> {

    /** 컨텐츠 타입: 저널 챕터 */
    @Builder.Default
    private String contentType = ContentType.JOURNAL_CHAPTER.key;

    /** 챕터 타입 (DIARY | DREAM) */
    @Builder.Default
    private ChapterType chapterType = ChapterType.DIARY;

    /** 제목 */
    private String title;
    /** 카테고리 코드: join 없이 화면 표시용으로 사용 */
    private String categoryCode;
    /** 카테고리 이름: join 없이 화면 표시용으로 사용 */
    private String categoryName;

    /** 마크다운 변환용 원문 */
    private String markdownContent;

    /** 정렬순서 변경 여부 */
    @Builder.Default
    private Boolean isSortOrderChanged = false;

    /* ----- */

    /** 저널 일자 ID */
    private Integer journalDayId;
    /** 기준 일자 */
    private String stdrdDt;
    /** 저널 일자 요일 */
    private String journalDateWeekDay;
    /** 연도 */
    private Integer yy;
    /** 월 */
    private Integer mnth;
    /** 정렬순서 */
    private Integer sortOrder;

    /** 저널 엔트리 목록 */
    private List<JournalEntryDto> journalEntryList;
    /* ----- */

    /**
     * 기준 일자 기준으로 비교한다.
     *
     * @param other 비교 대상
     * @return 기준 일자 비교 결과
     */
    @SneakyThrows
    @Override
    public int compareTo(final @NotNull JournalChapterDto other) {
        final Date thisDate = DateUtils.asDate(this.getStdrdDt());
        if (thisDate == null) return -1;

        final Date otherDate = DateUtils.asDate(other.getStdrdDt());
        return thisDate.compareTo(otherDate);
    }

    @Override
    public Integer getKey() {
        return this.id;
    }

    /** 태그 컴포지션 모듈.
     * 자체 태그 저장은 하지 않는다(엔티티 TagEmbed 제거). 이 필드는 화면 표시용 집계 컨테이너로,
     * JournalDayViewHelper.applyChapterTagSummary 가 소속 diary 엔트리 태그를 여기에 집계해 넣는다.
     * 엔티티에서 매핑되지 않으므로 non-null 로 초기화해 집계 시 NPE 를 막는다. */
    @Builder.Default
    public TagCmpstn tag = new TagCmpstn();
    /** 상태 컴포지션 모듈 */
    public StateCmpstn state;
}
