package io.nicheblog.dreamdiary.feature.journal._shared.model;

import io.nicheblog.dreamdiary.feature.journal.annual.model.JournalAnnualDto;
import io.nicheblog.dreamdiary.feature.journal.annual.model.JournalAnnualReviewDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryDto;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryPostDto;
import io.nicheblog.dreamdiary.feature.journal.dream.model.JournalDreamDto;
import io.nicheblog.dreamdiary.feature.journal.interpretation.model.JournalInterpretationDto;
import io.nicheblog.dreamdiary.feature.journal.todo.model.JournalTodoDto;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.*;

/**
 * JournalCacheEvictParam
 * <pre>
 *  저널 캐시 초기화 관련 필요 인자 파라미터 객체
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
    /** 등록자 ID */
    private String createdBy;
    /** 글 번호 */
    private Integer id;
    /** 저널 일자 번호 */
    private Integer journalDayId;
    /** 저널 챕터 번호 */
    private Integer journalChapterId;
    /** 저널 꿈 번호 */
    private Integer journalDreamId;
    /** 저널 결산 번호 */
    private Integer journalAnnualId;
    /** 년도 */
    private Integer yy;
    /** 월 */
    private Integer mnth;
    /** 주 시작일자 */
    private String weekStartDt;
    /** 주 시작일자 (수정 전) */
    private String prevWeekStartDt;

    /**
     * 팩토리 메서드 패턴
     *
     * @param dto {@link JournalDayDto}
     * @return {@link JournalCacheEvictParam}
     */
    public static JournalCacheEvictParam of(final JournalDayDto dto) {
        return JournalCacheEvictParam.builder()
                .createdBy(dto.getCreatedBy())
                .id(dto.getId())
                .yy(dto.getYy())
                .mnth(dto.getMnth())
                .weekStartDt(dto.getWeekStartDt())
                .build();
    }

    /**
     * 팩토리 메서드 패턴
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
     * 팩토리 메서드 패턴
     *
     * @param dto {@link JournalChapterDto}
     * @return {@link JournalCacheEvictParam}
     */
    public static JournalCacheEvictParam of(final JournalChapterDto dto) throws Exception {
        return JournalCacheEvictParam.builder()
                .createdBy(dto.getCreatedBy())
                .id(dto.getId())
                .journalDayId(dto.getJournalDayId())
                .yy(dto.getYy())
                .mnth(dto.getMnth())
                .weekStartDt(DateUtils.getWeekStartDateStr(dto.getStdrdDt()))
                .build();
    }

    /**
     * 팩토리 메서드 패턴
     *
     * @param dto {@link JournalDiaryDto}
     * @return {@link JournalCacheEvictParam}
     */
    public static JournalCacheEvictParam of(final JournalDiaryPostDto dto) {
        return JournalCacheEvictParam.builder()
                .createdBy(dto.getCreatedBy())
                .id(dto.getId())
                .journalDayId(dto.getJournalDayId())
                .journalChapterId(dto.getJournalChapterId())
                .yy(dto.getYy())
                .mnth(dto.getMnth())
                .build();
    }

    /**
     * 팩토리 메서드 패턴
     *
     * @param dto {@link JournalDayDto}
     * @return {@link JournalCacheEvictParam}
     */
    public static JournalCacheEvictParam of(final JournalDiaryDto dto) throws Exception {
        return JournalCacheEvictParam.builder()
                .createdBy(dto.getCreatedBy())
                .id(dto.getId())
                .journalDayId(dto.getJournalDayId())
                .journalChapterId(dto.getJournalChapterId())
                .yy(dto.getYy())
                .mnth(dto.getMnth())
                .weekStartDt(DateUtils.getWeekStartDateStr(dto.getStdrdDt()))
                .build();
    }

    /**
     * 팩토리 메서드 패턴
     *
     * @param dto {@link JournalDreamDto}
     * @return {@link JournalCacheEvictParam}
     */
    public static JournalCacheEvictParam of(final JournalDreamDto dto) throws Exception {
        return JournalCacheEvictParam.builder()
                .createdBy(dto.getCreatedBy())
                .id(dto.getId())
                .journalDayId(dto.getJournalDayId())
                .yy(dto.getYy())
                .mnth(dto.getMnth())
                .weekStartDt(DateUtils.getWeekStartDateStr(dto.getStdrdDt()))
                .build();
    }

    /**
     * 팩토리 메서드 패턴
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
                .weekStartDt(DateUtils.getWeekStartDateStr(dto.getStdrdDt()))
                .build();
    }

    /**
     * 팩토리 메서드 패턴
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
     * 팩토리 메서드 패턴
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
     * 팩토리 메서드 패턴
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

