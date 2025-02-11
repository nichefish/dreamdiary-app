package io.nicheblog.dreamdiary.domain.jrnl.day.model;

import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.domain.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseClsfDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
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
 * JrnlDayDto
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
public class JrnlDayDto
        extends BaseClsfDto
        implements Identifiable<Integer>, TagCmpstnModule, Comparable<JrnlDayDto>  {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JRNL_DAY;
    /** 필수(Override): 글분류 코드 */
    @Builder.Default
    private static final String CTGR_CL_CD = CONTENT_TYPE.name() + "_CTGR_CD";

    /** 컨텐츠 타입 */
    @Builder.Default
    private String contentType = CONTENT_TYPE.key;

    /* ----- */

    /** 저널 일자 */
    @Size(max = 10, message = "일자는 최대 10자여야 합니다.")
    @Pattern(regexp = "(\\d{4}-\\d{2}-\\d{2}|\\s*)", message = "일자는 'YYYY-MM-DD' 형식이어야 합니다.")
    private String jrnlDt;

    /** 저널 일자 요일 */
    private String jrnlDtWeekDay;

    /** 날짜미상 여부 (Y/N) */
    @Builder.Default
    @Size(min = 1, max = 1)
    @Pattern(regexp = "^[YN]$")
    private String dtUnknownYn = "N";

    /** 대략일자 (날짜미상시 해당일자 이후에 표기) */
    @Size(max = 10, message = "일자는 최대 10자여야 합니다.")
    @Pattern(regexp = "(\\d{4}-\\d{2}-\\d{2}|\\s*)", message = "일자는 'YYYY-MM-DD' 형식이어야 합니다.")
    private String aprxmtDt;

    /** 기준일자 (저널일자 또는 대략일자) */
    private String stdrdDt;

    /** 년도 */
    private String yy;
    /** 월 */
    private String mnth;

    /** 날씨 */
    @Size(max = 100, message = "날씨 정보는 100자 이하로 입력해야 합니다.")
    private String weather;

    /** 저널 일기 목록 */
    private List<JrnlDiaryDto> jrnlDiaryList;

    /** 저널 꿈 목록 */
    private List<JrnlDreamDto> jrnlDreamList;

    /** 저널 꿈 (타인) 목록 */
    private List<JrnlDreamDto> jrnlElseDreamList;

    /* ----- */

    /**
     * Getter :: 기준일자
     */
    public String getStdrdDt() {
        if (!StringUtils.isEmpty(this.jrnlDt)) return jrnlDt;
        return aprxmtDt;
    }

    /**
     * Getter :: 꿈 목록 보유 여부
     */
    public Boolean getHasDream() {
        return !CollectionUtils.isEmpty(this.jrnlDreamList) || !CollectionUtils.isEmpty(this.jrnlElseDreamList);
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
    public int compareTo(final @NotNull JrnlDayDto other) {
        Date thisDate = DateUtils.asDate(this.getStdrdDt());
        if (thisDate == null) return -1;

        Date otherDate = DateUtils.asDate(other.getStdrdDt());
        return thisDate.compareTo(otherDate);
    }

    @Override
    public Integer getKey() {
        return this.postNo;
    }

    /** 위임 :: 태그 정보 모듈 */
    public TagCmpstn tag;
}
