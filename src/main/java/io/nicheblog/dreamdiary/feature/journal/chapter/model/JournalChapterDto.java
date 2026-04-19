package io.nicheblog.dreamdiary.feature.journal.chapter.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.state.model.cmpstn.StateCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalPeriodModule;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryDto;
import io.nicheblog.dreamdiary.feature.journal.note.model.JournalNoteDto;
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

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private String contentType = ContentType.JOURNAL_CHAPTER.key;

    /** 챕터 타입 (DIARY | DREAM) */
    @Builder.Default
    private ChapterType chapterType = ChapterType.DIARY;

    /** 제목 */
    private String title;
    /** 글분류 코드 :: join을 제거하고 메모리 캐시 처리 */
    private String categoryCode;
    /** 글분류 코드 이름 :: join을 제거하고 메모리 캐시 처리 */
    private String categoryName;

    /** 마크다운 처리된 내용 */
    private String markdownContent;

    /** 인덱스 변경 여부 */
    @Builder.Default
    private Boolean isSortOrderChanged = false;

    /* ----- */

    /** 저널 일자 번호 */
    private Integer journalDayId;
    /** 저널 기준일자 */
    private String stdrdDt;
    /** 저널 일자 요일 */
    private String journalDateWeekDay;
    /** 저널 기준일자 */
    private Integer yy;
    /** 저널 기준일자 */
    private Integer mnth;
    /** 순번 */
    private Integer sortOrder;

    /** 저널 일기 목록 */
    private List<JournalDiaryDto> journalDiaryList;

    /** 저널 노트 목록 */
    private List<JournalNoteDto> journalNoteList;

    /* ----- */

    /**
     * 날짜 오름차순 정렬
     *
     * @param other - 비교할 객체
     * @return 양수: 현재 객체가 더 큼, 음수: 현재 객체가 더 작음, 0: 두 객체가 같음
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

    /** 위임 :: 태그 정보 모듈 */
    public TagCmpstn tag;
    /** 위임 :: 상태 정보 모듈 */
    public StateCmpstn state;
}

