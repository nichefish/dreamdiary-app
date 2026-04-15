package io.nicheblog.dreamdiary.feature.journal.day.model;

import io.nicheblog.dreamdiary.feature.clsf._shared.model.BaseClsfDto;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.meta.model.cmpstn.MetaCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.meta.model.cmpstn.MetaCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalPeriodModule;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterCtgrHintDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterSmpDto;
import io.nicheblog.dreamdiary.feature.journal.dream.model.JournalDreamDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.global.validator.state.UpdateState;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

/**
 * JournalDayDto
 * <pre>
 *  저널 일자 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JournalDayDto
        extends BaseClsfDto
        implements Identifiable<Integer>, TagCmpstnModule, MetaCmpstnModule, JournalPeriodModule, Comparable<JournalDayDto>  {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private String contentType = ContentType.JOURNAL_DAY.key;

    /* ----- */

    /** 저널 일자 */
    @Size(max = 10, message = "일자는 최대 10자여야 합니다.")
    @Pattern(regexp = "(\\d{4}-\\d{2}-\\d{2}|\\s*)", message = "일자는 'YYYY-MM-DD' 형식이어야 합니다.")
    private String journalDt;

    /** 저널 일자 요일 */
    private String journalDtWeekDay;

    /** 날짜미상 여부 (Y/N) */
    @Builder.Default
    @Pattern(regexp = "^[YN]$", groups = UpdateState.class)
    private String dtUnknownYn = "N";

    /** 대략일자 (날짜미상시 해당일자 이후에 표기) */
    @Size(max = 10, message = "일자는 최대 10자여야 합니다.")
    @Pattern(regexp = "(\\d{4}-\\d{2}-\\d{2}|\\s*)", message = "일자는 'YYYY-MM-DD' 형식이어야 합니다.")
    private String aprxmtDt;

    /** 기준일자 (저널일자 또는 대략일자) */
    private String stdrdDt;

    /** 년도 */
    private Integer yy;
    /** 월 */
    private Integer mnth;
    /** 주 시작일자 (월요일 기준) */
    private String weekStartDt;
    /** 이전 주 시작일자 */
    private String prevWeekStartDt;

    /** 공휴일 여부 */
    private Boolean isHolyday;
    /** 공휴일 이름 */
    private String holydayNm;

    /** 날씨 */
    @Size(max = 100, message = "날씨 정보는 100자 이하로 입력해야 합니다.")
    private String weather;

    /** 저널 챕터 목록 */
    private List<JournalChapterDto> journalChapterList;
    /** 저널 챕터 목록 */
    private List<JournalChapterSmpDto> chapterList;
    /** 챕터 필터로 숨겨진 카테고리 목록 */
    private List<JournalChapterCtgrHintDto> hiddenChapterCtgrList;

    /** 저널 꿈 목록 */
    private List<JournalDreamDto> journalDreamList;

    /** 저널 꿈 (타인) 목록 */
    private List<JournalDreamDto> journalElseDreamList;

    /* ----- */

    /**
     * Getter :: 기준일자
     */
    public String getStdrdDt() {
        if (!StringUtils.isEmpty(this.journalDt)) return journalDt;
        return aprxmtDt;
    }

    /**
     * Getter :: 꿈 목록 보유 여부
     */
    public Boolean getHasDream() {
        return !CollectionUtils.isEmpty(this.journalDreamList) || !CollectionUtils.isEmpty(this.journalElseDreamList);
    }

    /* ----- */

    /**
     * 날짜 오름차순 정렬
     *
     * @param other - 비교할 객체
     * @return 양수: 현재 객체가 더 큼, 음수: 현재 객체가 더 작음, 0: 두 객체가 같음
     */
    @SneakyThrows
    @Override
    public int compareTo(final @NotNull JournalDayDto other) {
        Date thisDate = DateUtils.asDate(this.getStdrdDt());
        if (thisDate == null) return -1;

        Date otherDate = DateUtils.asDate(other.getStdrdDt());
        return thisDate.compareTo(otherDate);
    }

    @Override
    public Integer getKey() {
        return this.id;
    }

    /** 위임 :: 태그 정보 모듈 */
    public TagCmpstn tag;
    /** 위임 :: 메타 정보 모듈 */
    public MetaCmpstn meta;
}

