package io.nicheblog.dreamdiary.feature.journal.dream.model;

import io.nicheblog.dreamdiary.feature.clsf._shared.model.BaseClsfDto;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.file.model.cmpstn.AtchFileCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.file.model.cmpstn.AtchFileCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.clsf.history.model.cmpstn.HistoryCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.history.model.cmpstn.HistoryCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.related.model.RelatedContentDto;
import io.nicheblog.dreamdiary.feature.clsf.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.state.model.cmpstn.StateCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalPeriodModule;
import io.nicheblog.dreamdiary.feature.journal.intrpt.model.JournalIntrptDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.global.validator.state.UpdateState;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;

import javax.validation.constraints.Pattern;
import java.util.Date;
import java.util.List;

/**
 * JournalDreamDto
 * <pre>
 *  저널 꿈 Dto.
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
public class JournalDreamDto
        extends BaseClsfDto
        implements Identifiable<Integer>, AtchFileCmpstnModule, CommentCmpstnModule, TagCmpstnModule, StateCmpstnModule, HistoryCmpstnModule, JournalPeriodModule, Comparable<JournalDreamDto> {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private String contentType = ContentType.JOURNAL_DREAM.key;

    /** 제목 */
    private String title;
    /** 내용 */
    private String content;
    /** 마크다운 처리된 내용 */
    private String markdownContent;

    /* ----- */

    /** 저널 일자 번호 */
    private Integer journalDayId;
    /** 저널 기준일자 */
    private String stdrdDt;
    /** 저널 기준일자 */
    private String dtUnknownYn;
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

    /** 순번 */
    private Integer sortOrder;

    /** 저널 일기 목록 */
    private List<JournalIntrptDto> journalIntrptList;

    /** 악몽 여부 (Y/N) */
    @Builder.Default
    @Pattern(regexp = "^[YN]$", groups = UpdateState.class)
    private String nhtmrYn = "N";
    /** 입면환각 여부 (Y/N) */
    @Builder.Default
    @Pattern(regexp = "^[YN]$", groups = UpdateState.class)
    private String hallucYn = "N";
    /** 타인 꿈 여부 (Y/N) */
    @Builder.Default
    @Pattern(regexp = "^[YN]$", groups = UpdateState.class)
    private String elseDreamYn = "N";
    /** 꿈꾼이(타인) 이름 */
    private String elseDreamerNm;

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
    public int compareTo(final @NotNull JournalDreamDto other) {
        Date thisDate = DateUtils.asDate(this.getStdrdDt());
        if (thisDate == null) return -1;

        Date otherDate = DateUtils.asDate(other.getStdrdDt());
        return thisDate.compareTo(otherDate);
    }

    @Override
    public Integer getKey() {
        return this.id;
    }

    /** 위임 :: 첨부파일 모듈 */
    public AtchFileCmpstn file;
    /** 위임 :: 댓글 정보 모듈 */
    public CommentCmpstn comment;
    /** 위임 :: 태그 정보 모듈 */
    public TagCmpstn tag;
    /** 위임 :: 상태 정보 모듈 */
    public StateCmpstn state;
    /** 위임 :: 이력 정보 모듈 */
    public HistoryCmpstn history;
    private List<HistoryDto> historyList;
    @Builder.Default
    private List<RelatedContentDto> relatedContentList = List.of();
}

