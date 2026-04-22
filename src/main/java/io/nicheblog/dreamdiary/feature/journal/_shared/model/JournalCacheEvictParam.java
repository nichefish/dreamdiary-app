package io.nicheblog.dreamdiary.feature.journal._shared.model;

import io.nicheblog.dreamdiary.feature.journal.annual.model.JournalAnnualDto;
import io.nicheblog.dreamdiary.feature.journal.annual.model.JournalAnnualReviewDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryPostDto;
import io.nicheblog.dreamdiary.feature.journal.interpretation.model.JournalInterpretationDto;
import io.nicheblog.dreamdiary.feature.journal.todo.model.JournalTodoDto;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.*;

/**
 * JournalCacheEvictParam
 * <pre>
 *  ???罹먯떆 珥덇린??愿???꾩슂 ?몄옄 ?뚮씪誘명꽣 媛앹껜
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JournalCacheEvictParam {
    /** ?깅줉??ID */
    private String createdBy;
    private String contentType;
    /** 湲 踰덊샇 */
    private Integer id;
    /** ????쇱옄 踰덊샇 */
    private Integer journalDayId;
    /** ???梨뺥꽣 踰덊샇 */
    private Integer journalChapterId;
    /** ???轅?踰덊샇 */
    private Integer journalDreamId;
    /** ???寃곗궛 踰덊샇 */
    private Integer journalAnnualId;
    /** ?꾨룄 */
    private Integer yy;
    /** ??*/
    private Integer mnth;
    /** 二??쒖옉?쇱옄 */
    private String weekStartDt;
    /** 二??쒖옉?쇱옄 (?섏젙 ?? */
    private String prevWeekStartDt;

    /**
     * ?⑺넗由?硫붿꽌???⑦꽩
     *
     * @param dto {@link JournalDayDto}
     * @return {@link JournalCacheEvictParam}
     */
    public static JournalCacheEvictParam of(final JournalDayDto dto) {
        return JournalCacheEvictParam.builder()
                .createdBy(dto.getCreatedBy())
                .contentType(dto.getContentType())
                .id(dto.getId())
                .yy(dto.getYy())
                .mnth(dto.getMnth())
                .weekStartDt(dto.getWeekStartDt())
                .build();
    }

    /**
     * ?⑺넗由?硫붿꽌???⑦꽩
     *
     * @param postDto {@link JournalDayDto}
     * @param updatedDto {@link JournalDayDto}
     * @return {@link JournalCacheEvictParam}
     */
    public static JournalCacheEvictParam of(final JournalDayDto postDto, final JournalDayDto updatedDto) {
        return JournalCacheEvictParam.builder()
                .createdBy(updatedDto.getCreatedBy())
                .id(updatedDto.getId())
                .yy(updatedDto.getYy())
                .mnth(updatedDto.getMnth())
                .weekStartDt(updatedDto.getWeekStartDt())
                .prevWeekStartDt(postDto.getPrevWeekStartDt())
                .build();
    }

    /**
     * ?⑺넗由?硫붿꽌???⑦꽩
     *
     * @param dto {@link JournalChapterDto}
     * @return {@link JournalCacheEvictParam}
     */
    public static JournalCacheEvictParam of(final JournalChapterDto dto) throws Exception {
        return JournalCacheEvictParam.builder()
                .createdBy(dto.getCreatedBy())
                .contentType(dto.getContentType())
                .id(dto.getId())
                .journalDayId(dto.getJournalDayId())
                .yy(dto.getYy())
                .mnth(dto.getMnth())
                .weekStartDt(DateUtils.getWeekStartDateStr(dto.getStdrdDt()))
                .build();
    }

    /**
     * ?⑺넗由?硫붿꽌???⑦꽩
     *
     * @param dto {@link JournalEntryDto}
     * @return {@link JournalCacheEvictParam}
     */
    public static JournalCacheEvictParam of(final JournalEntryPostDto dto) {
        return JournalCacheEvictParam.builder()
                .createdBy(dto.getCreatedBy())
                .contentType(dto.getContentType())
                .id(dto.getId())
                .journalDayId(dto.getJournalDayId())
                .journalChapterId(dto.getJournalChapterId())
                .yy(dto.getYy())
                .mnth(dto.getMnth())
                .build();
    }

    /**
     * ?⑺넗由?硫붿꽌???⑦꽩
     *
     * @param dto {@link JournalDayDto}
     * @return {@link JournalCacheEvictParam}
     */
/**
     * ?⑺넗由?硫붿꽌???⑦꽩
     *
     * @param dto {@link JournalEntryDto}
     * @return {@link JournalCacheEvictParam}
     */
    public static JournalCacheEvictParam of(final JournalEntryDto dto) throws Exception {
        return JournalCacheEvictParam.builder()
                .createdBy(dto.getCreatedBy())
                .contentType(dto.getContentType())
                .id(dto.getId())
                .journalDayId(dto.getJournalDayId())
                .yy(dto.getYy())
                .mnth(dto.getMnth())
                .weekStartDt(DateUtils.getWeekStartDateStr(dto.getStdrdDt()))
                .build();
    }
/**
     * ?⑺넗由?硫붿꽌???⑦꽩
     *
     * @param dto {@link JournalInterpretationDto}
     * @return {@link JournalCacheEvictParam}
     */
    public static JournalCacheEvictParam of(final JournalInterpretationDto dto) throws Exception {
        return JournalCacheEvictParam.builder()
                .createdBy(dto.getCreatedBy())
                .id(dto.getId())
                .journalDayId(dto.getJournalDayId())
                .yy(dto.getYy())
                .mnth(dto.getMnth())
                .weekStartDt(dto.getStdrdDt() == null ? null : DateUtils.getWeekStartDateStr(dto.getStdrdDt()))
                .build();
    }

    /**
     * ?⑺넗由?硫붿꽌???⑦꽩
     *
     * @param dto {@link JournalTodoDto}
     * @return {@link JournalCacheEvictParam}
     */
    public static JournalCacheEvictParam of(final JournalAnnualDto dto) {
        return JournalCacheEvictParam.builder()
                .createdBy(dto.getCreatedBy())
                .id(dto.getId())
                .yy(dto.getYy())
                .build();
    }

    /**
     * ?⑺넗由?硫붿꽌???⑦꽩
     *
     * @param dto {@link JournalTodoDto}
     * @return {@link JournalCacheEvictParam}
     */
    public static JournalCacheEvictParam of(final JournalAnnualReviewDto dto) {
        return JournalCacheEvictParam.builder()
                .createdBy(dto.getCreatedBy())
                .id(dto.getId())
                .journalAnnualId(dto.getJournalAnnualId())
                .yy(dto.getYy())
                .build();
    }

    /**
     * ?⑺넗由?硫붿꽌???⑦꽩
     *
     * @param dto {@link JournalTodoDto}
     * @return {@link JournalCacheEvictParam}
     */
    public static JournalCacheEvictParam of(final JournalTodoDto dto) {
        return JournalCacheEvictParam.builder()
                .createdBy(dto.getCreatedBy())
                .id(dto.getId())
                .yy(dto.getYy())
                .mnth(dto.getMnth())
                .build();
    }

}

