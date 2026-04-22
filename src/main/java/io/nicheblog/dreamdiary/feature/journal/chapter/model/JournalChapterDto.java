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
 *  ????筌?벤苑?Dto.
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

    /** ?袁⑸땾: ?뚢뫂?쀯㎘?????*/
    @Builder.Default
    private String contentType = ContentType.JOURNAL_CHAPTER.key;

    /** 筌?벤苑?????(DIARY | DREAM) */
    @Builder.Default
    private ChapterType chapterType = ChapterType.DIARY;

    /** ??뺛걠 */
    private String title;
    /** 疫꼲?브쑬履??꾨뗀諭?:: join????볤탢??랁?筌롫뗀?덄뵳?筌?Ŋ??筌ｌ꼶??*/
    private String categoryCode;
    /** 疫꼲?브쑬履??꾨뗀諭???已?:: join????볤탢??랁?筌롫뗀?덄뵳?筌?Ŋ??筌ｌ꼶??*/
    private String categoryName;

    /** 筌띾뜇寃??쇱뒲 筌ｌ꼶?????곸뒠 */
    private String markdownContent;

    /** ?紐껊쑔??癰궰野???? */
    @Builder.Default
    private Boolean isSortOrderChanged = false;

    /* ----- */

    /** ??????깆쁽 甕곕뜇??*/
    private Integer journalDayId;
    /** ????疫꿸퀣???깆쁽 */
    private String stdrdDt;
    /** ??????깆쁽 ?遺우뵬 */
    private String journalDateWeekDay;
    /** ????疫꿸퀣???깆쁽 */
    private Integer yy;
    /** ????疫꿸퀣???깆쁽 */
    private Integer mnth;
    /** ??뺤쓰 */
    private Integer sortOrder;

    /** ??????⑤┛ 筌뤴뫖以?*/
    private List<JournalEntryDto> journalEntryList;
    /* ----- */

    /**
     * ?醫롮? ??살カ筌△뫁???類ｌ졊
     *
     * @param other - ??쑨???揶쏆빘猿?     * @return ?臾믩땾: ?袁⑹삺 揶쏆빘猿쒎첎? ???? ???땾: ?袁⑹삺 揶쏆빘猿쒎첎? ???臾믪벉, 0: ??揶쏆빘猿쒎첎? 揶쏆늿??
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

    /** ?袁⑹뿫 :: ??볥젃 ?類ｋ궖 筌뤴뫀諭?*/
    public TagCmpstn tag;
    /** ?袁⑹뿫 :: ?怨밴묶 ?類ｋ궖 筌뤴뫀諭?*/
    public StateCmpstn state;
}


