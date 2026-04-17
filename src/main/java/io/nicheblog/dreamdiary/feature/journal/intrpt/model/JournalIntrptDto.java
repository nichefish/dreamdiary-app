package io.nicheblog.dreamdiary.feature.journal.intrpt.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.attachable.history.model.cmpstn.HistoryCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.history.model.cmpstn.HistoryCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.state.model.cmpstn.StateCmpstnModule;
import io.nicheblog.dreamdiary.feature.file.model.cmpstn.FileCmpstn;
import io.nicheblog.dreamdiary.feature.file.model.cmpstn.FileCmpstnModule;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalPeriodModule;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

/**
 * JournalIntrptDto
 * <pre>
 *  저널 해석 Dto.
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
public class JournalIntrptDto
        extends BaseAttachableDto
        implements Identifiable<Integer>, FileCmpstnModule, CommentCmpstnModule, StateCmpstnModule, HistoryCmpstnModule, JournalPeriodModule, Comparable<JournalIntrptDto> {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private String contentType = ContentType.JOURNAL_INTRPT.key;

    /* ----- */

    /** 저널 일자 번호 */
    private Integer journalDayId;
    /** 저널 꿈 번호 */
    private Integer journalDreamId;
    /** 저널 기준일자 */
    private String stdrdDt;
    /** 저널 일자 요일 */
    private String journalDtWeekDay;

    /** 저널 기준일자 */
    private Integer yy;
    /** 저널 기준일자 */
    private Integer mnth;

    /** 공휴일 여부 */
    private Boolean isHolyday;
    /** 공휴일 이름 */
    private String holydayNm;

    /** 제목 */
    private String title;
    /** 내용 */
    private String content;
    /** 마크다운 처리된 내용 */
    private String markdownContent;

    /** 순번 */
    private Integer sortOrder;

    /** 인덱스 변경 여부 */
    @Builder.Default
    private Boolean isSortOrderChanged = false;

    /* ----- */

    /**
     * 날짜 오름차순 정렬
     *
     * @param other - 비교할 객체
     * @return 양수: 현재 객체가 더 큼, 음수: 현재 객체가 더 작음, 0: 두 객체가 같음
     */
    @SneakyThrows
    @Override
    public int compareTo(final @NotNull JournalIntrptDto other) {
        Date thisDate = DateUtils.asDate(this.getStdrdDt());
        if (thisDate == null) return -1;

        Date otherDate = DateUtils.asDate(other.getStdrdDt());
        return thisDate.compareTo(otherDate);
    }

    /* ----- */

    @Override
    public Integer getKey() {
        return this.id;
    }

    /** 위임 :: 첨부파일 모듈 */
    public FileCmpstn file;
    /** 위임 :: 댓글 정보 모듈 */
    public CommentCmpstn comment;
    /** 위임 :: 상태 정보 모듈 */
    public StateCmpstn state;
    public HistoryCmpstn history;
    private List<HistoryDto> historyList;
}

