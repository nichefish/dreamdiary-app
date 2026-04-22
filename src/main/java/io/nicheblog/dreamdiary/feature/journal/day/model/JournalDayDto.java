package io.nicheblog.dreamdiary.feature.journal.day.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.meta.model.cmpstn.MetaCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.meta.model.cmpstn.MetaCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalPeriodModule;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterCtgrHintDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterSmpDto;
import io.nicheblog.dreamdiary.feature.journal.day.type.JournalDatePrecision;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
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
 *  ????쇱옄 Dto.
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

    /** ?꾩닔: 而⑦뀗痢????*/
    @Builder.Default
    private String contentType = ContentType.JOURNAL_DAY.key;

    /* ----- */

    /** ????쇱옄 */
    @Size(max = 10, message = "?쇱옄??理쒕? 10?먯뿬???⑸땲??")
    @Pattern(regexp = "(\\d{4}-\\d{2}-\\d{2}|\\s*)", message = "?쇱옄??'YYYY-MM-DD' ?뺤떇?댁뼱???⑸땲??")
    private String journalDate;

    /** ????쇱옄 ?붿씪 */
    private String journalDateWeekDay;

    /** ????좎쭨 ?뺣???(EXACT | APPROXIMATE | UNKNOWN) */
    @Builder.Default
    private JournalDatePrecision journalDatePrecision = JournalDatePrecision.EXACT;

    /** 湲곗??쇱옄 (??먯씪???먮뒗 ??듭씪?? */
    private String stdrdDt;

    /** ?꾨룄 */
    private Integer yy;
    /** ??*/
    private Integer mnth;
    /** 二??쒖옉?쇱옄 (?붿슂??湲곗?) */
    private String weekStartDt;
    /** ?댁쟾 二??쒖옉?쇱옄 */
    private String prevWeekStartDt;

    /** 怨듯쑕???щ? */
    private Boolean isHolyday;
    /** 怨듯쑕???대쫫 */
    private String holydayNm;

    /** ?좎뵪 */
    @Size(max = 100, message = "?좎뵪 ?뺣낫??100???댄븯濡??낅젰?댁빞 ?⑸땲??")
    private String weather;

    /** ???梨뺥꽣 紐⑸줉 */
    private List<JournalChapterDto> journalChapterList;
    /** ???梨뺥꽣 紐⑸줉 */
    private List<JournalChapterSmpDto> chapterList;
    /** 梨뺥꽣 ?꾪꽣濡??④꺼吏?移댄뀒怨좊━ 紐⑸줉 */
    private List<JournalChapterCtgrHintDto> hiddenChapterCtgrList;

    /** ???轅?紐⑸줉 */
    private List<JournalEntryDto> journalDreamList;

    /** ???轅?(??? 紐⑸줉 */
    private List<JournalEntryDto> journalElseDreamList;

    /* ----- */

    /**
     * Getter :: 湲곗??쇱옄
     */
    public String getStdrdDt() {
        return journalDate;
    }

    /**
     * Getter :: 轅?紐⑸줉 蹂댁쑀 ?щ?
     */
    public Boolean getHasDream() {
        return !CollectionUtils.isEmpty(this.journalDreamList) || !CollectionUtils.isEmpty(this.journalElseDreamList);
    }

    /* ----- */

    /**
     * ?좎쭨 ?ㅻ쫫李⑥닚 ?뺣젹
     *
     * @param other - 鍮꾧탳??媛앹껜
     * @return ?묒닔: ?꾩옱 媛앹껜媛 ???? ?뚯닔: ?꾩옱 媛앹껜媛 ???묒쓬, 0: ??媛앹껜媛 媛숈쓬
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

    /** ?꾩엫 :: ?쒓렇 ?뺣낫 紐⑤뱢 */
    public TagCmpstn tag;
    /** ?꾩엫 :: 硫뷀? ?뺣낫 紐⑤뱢 */
    public MetaCmpstn meta;
}


