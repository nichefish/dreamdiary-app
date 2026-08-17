package io.nicheblog.dreamdiary.feature.journal.day.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.meta.model.cmpstn.MetaCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.meta.model.cmpstn.MetaCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalPeriodModule;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterPrefixHintDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterSmpDto;
import io.nicheblog.dreamdiary.feature.journal.day.type.JournalDatePrecision;
import io.nicheblog.dreamdiary.feature.calendar.schedule.type.VacationDayStatus;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;
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
        extends BaseAttachableDto
        implements Identifiable<Integer>, TagCmpstnModule, MetaCmpstnModule, JournalPeriodModule, Comparable<JournalDayDto>  {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private String contentType = ContentType.JOURNAL_DAY.key;

    /** 저널 일자 소유 사용자 영속 ID. 서버가 인증 사용자 기준으로 확정한다. */
    private Integer ownerId;

    /* ----- */

    /** 저널 일자 */
    @Size(max = 10, message = "{msg.journal.day.date.max-length}")
    @Pattern(regexp = "(\\d{4}-\\d{2}-\\d{2}|\\s*)", message = "{msg.journal.day.date.format}")
    private String journalDate;

    /** 저널 일자 요일 */
    private String journalDateWeekDay;

    /** 저널 날짜 정밀도 (EXACT | APPROXIMATE | UNKNOWN) */
    @Builder.Default
    private JournalDatePrecision journalDatePrecision = JournalDatePrecision.EXACT;

    /** 기준일자 (저널일자 또는 대표일자) */
    private String stdrdDt;

    /** 연도 */
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

    /** 현재 사용자 참가 휴가의 일자 시간 범위 상태. 전역 공휴일·주말과 별도 축이다. */
    @Builder.Default
    private VacationDayStatus vacationDayStatus = VacationDayStatus.NONE;

    /** 해당 일자와 겹치는 현재 사용자 휴가 일정 제목 목록. */
    @Builder.Default
    private List<String> vacationReasonList = List.of();

    /** 날씨 */
    @Size(max = 100, message = "{msg.journal.day.weather.max-length}")
    private String weather;

    /**
     * 일기 축 완결 여부 (Y/N).
     * <p>Y이면 일기·노트 축 쓰기를 잠근다. 엔트리 RESOLVED 와 무관하며 수동 토글만 반영한다.</p>
     */
    @Builder.Default
    @Size(max = 1)
    private String diaryResolvedYn = "N";

    /**
     * 꿈 축 완결 여부 (Y/N).
     * <p>Y이면 꿈 축 쓰기를 잠근다. 엔트리 RESOLVED 와 무관하며 수동 토글만 반영한다.</p>
     */
    @Builder.Default
    @Size(max = 1)
    private String dreamResolvedYn = "N";

    /** 저널 챕터 목록 */
    private List<JournalChapterDto> journalChapterList;
    /** 저널 챕터 간단 목록 */
    private List<JournalChapterSmpDto> chapterList;
    /** 챕터 Prefix 필터로 숨겨진 말머리 목록 */
    private List<JournalChapterPrefixHintDto> hiddenChapterPrefixList;

    /** 저널 꿈 가상 섹션 (내 꿈 + 꿈꾼 이름별) */
    private List<JournalDreamSectionDto> journalDreamSectionList;

    /* ----- */

    /**
     * Getter :: 기준일자
     */
    public String getStdrdDt() {
        return journalDate;
    }

    /**
     * Getter :: 꿈 목록 보유 여부
     */
    public Boolean getHasDream() {
        return CollectionUtils.isNotEmpty(this.journalDreamSectionList);
    }

    /* ----- */

    /**
     * 날짜 오름차순 정렬
     *
     * @param other 비교할 객체
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
