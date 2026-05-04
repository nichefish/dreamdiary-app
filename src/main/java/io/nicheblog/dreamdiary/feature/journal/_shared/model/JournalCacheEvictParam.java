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
 *  저널 캐시 무효화에 필요한 공통 파라미터.
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

    /** 작성자 ID */
    private String createdBy;
    /** 컨텐츠 타입 */
    private String contentType;
    /** 글 번호 */
    private Integer id;
    /** 저널 일자 번호 */
    private Integer journalDayId;
    /** 저널 챕터 번호 */
    private Integer journalChapterId;
    /** 이전 저널 챕터 번호 */
    private Integer prevJournalChapterId;
    /** 저널 꿈 번호 */
    private Integer journalDreamId;
    /** 저널 결산 번호 */
    private Integer journalAnnualId;
    /** 연도 */
    private Integer yy;
    /** 월 */
    private Integer mnth;
    /** 주 시작일자 */
    private String weekStartDt;
    /** 이전 주 시작일자 */
    private String prevWeekStartDt;

    /**
     * 일자 DTO 기준 캐시 무효화 파라미터 생성.
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
     * 일자 수정 전후 DTO 기준 캐시 무효화 파라미터 생성.
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
     * 챕터 DTO 기준 캐시 무효화 파라미터 생성.
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
     * 엔트리 저장 DTO 기준 캐시 무효화 파라미터 생성.
     *
     * @param dto {@link JournalEntryPostDto}
     * @return {@link JournalCacheEvictParam}
     */
    public static JournalCacheEvictParam of(final JournalEntryPostDto dto) {
        return JournalCacheEvictParam.builder()
                .createdBy(dto.getCreatedBy())
                .contentType(dto.getContentType())
                .id(dto.getId())
                .journalDayId(dto.getJournalDayId())
                .journalChapterId(dto.getJournalChapterId())
                .prevJournalChapterId(dto.getPrevJournalChapterId())
                .yy(dto.getYy())
                .mnth(dto.getMnth())
                .build();
    }

    /**
     * 엔트리 수정 전후 DTO 기준 캐시 무효화 파라미터 생성.
     *
     * @param postDto 수정 요청 DTO
     * @param updatedDto 수정 결과 DTO
     * @return {@link JournalCacheEvictParam}
     */
    public static JournalCacheEvictParam of(final JournalEntryPostDto postDto, final JournalEntryDto updatedDto) throws Exception {
        return JournalCacheEvictParam.builder()
                .createdBy(updatedDto.getCreatedBy())
                .contentType(updatedDto.getContentType())
                .id(updatedDto.getId())
                .journalDayId(updatedDto.getJournalDayId())
                .journalChapterId(updatedDto.getJournalChapterId())
                .prevJournalChapterId(postDto.getPrevJournalChapterId())
                .yy(updatedDto.getYy())
                .mnth(updatedDto.getMnth())
                .weekStartDt(DateUtils.getWeekStartDateStr(updatedDto.getStdrdDt()))
                .build();
    }

    /**
     * 엔트리 DTO 기준 캐시 무효화 파라미터 생성.
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
                .journalChapterId(dto.getJournalChapterId())
                .prevJournalChapterId(dto.getPrevJournalChapterId())
                .yy(dto.getYy())
                .mnth(dto.getMnth())
                .weekStartDt(DateUtils.getWeekStartDateStr(dto.getStdrdDt()))
                .build();
    }

    /**
     * 해석 DTO 기준 캐시 무효화 파라미터 생성.
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
     * 결산 DTO 기준 캐시 무효화 파라미터 생성.
     *
     * @param dto {@link JournalAnnualDto}
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
     * 결산 리뷰 DTO 기준 캐시 무효화 파라미터 생성.
     *
     * @param dto {@link JournalAnnualReviewDto}
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
     * 할일 DTO 기준 캐시 무효화 파라미터 생성.
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
