package io.nicheblog.dreamdiary.feature.jrnl._shared.model;

import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryPostDto;
import io.nicheblog.dreamdiary.feature.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.feature.jrnl.chapter.model.JrnlChapterDto;
import io.nicheblog.dreamdiary.feature.jrnl.intrpt.model.JrnlIntrptDto;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.model.JrnlSumryDto;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.model.JrnlSumryReviewDto;
import io.nicheblog.dreamdiary.feature.jrnl.todo.model.JrnlTodoDto;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.*;

/**
 * JrnlCacheEvictParam
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
public class JrnlCacheEvictParam {
    /** 등록자 ID */
    private String regstrId;
    /** 글 번호 */
    private Integer id;
    /** 저널 일자 번호 */
    private Integer jrnlDayId;
    /** 저널 챕터 번호 */
    private Integer jrnlChapterId;
    /** 저널 꿈 번호 */
    private Integer jrnlDreamId;
    /** 저널 결산 번호 */
    private Integer jrnlSumryId;
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
     * @param dto {@link JrnlDayDto}
     * @return {@link JrnlCacheEvictParam}
     */
    public static JrnlCacheEvictParam of(final JrnlDayDto dto) {
        return JrnlCacheEvictParam.builder()
                .regstrId(dto.getRegstrId())
                .id(dto.getId())
                .yy(dto.getYy())
                .mnth(dto.getMnth())
                .weekStartDt(dto.getWeekStartDt())
                .build();
    }

    /**
     * 팩토리 메서드 패턴
     *
     * @param postDto {@link JrnlDayDto}
     * @param updatedDto {@link JrnlDayDto}
     * @return {@link JrnlCacheEvictParam}
     */
    public static JrnlCacheEvictParam of(final JrnlDayDto postDto, final JrnlDayDto updatedDto) {
        return JrnlCacheEvictParam.builder()
                .regstrId(updatedDto.getRegstrId())
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
     * @param dto {@link JrnlChapterDto}
     * @return {@link JrnlCacheEvictParam}
     */
    public static JrnlCacheEvictParam of(final JrnlChapterDto dto) throws Exception {
        return JrnlCacheEvictParam.builder()
                .regstrId(dto.getRegstrId())
                .id(dto.getId())
                .jrnlDayId(dto.getJrnlDayId())
                .yy(dto.getYy())
                .mnth(dto.getMnth())
                .weekStartDt(DateUtils.getWeekStartDateStr(dto.getStdrdDt()))
                .build();
    }

    /**
     * 팩토리 메서드 패턴
     *
     * @param dto {@link JrnlDiaryDto}
     * @return {@link JrnlCacheEvictParam}
     */
    public static JrnlCacheEvictParam of(final JrnlDiaryPostDto dto) {
        return JrnlCacheEvictParam.builder()
                .regstrId(dto.getRegstrId())
                .id(dto.getId())
                .jrnlDayId(dto.getJrnlDayId())
                .jrnlChapterId(dto.getJrnlChapterId())
                .yy(dto.getYy())
                .mnth(dto.getMnth())
                .build();
    }

    /**
     * 팩토리 메서드 패턴
     *
     * @param dto {@link JrnlDayDto}
     * @return {@link JrnlCacheEvictParam}
     */
    public static JrnlCacheEvictParam of(final JrnlDiaryDto dto) throws Exception {
        return JrnlCacheEvictParam.builder()
                .regstrId(dto.getRegstrId())
                .id(dto.getId())
                .jrnlDayId(dto.getJrnlDayId())
                .jrnlChapterId(dto.getJrnlChapterId())
                .yy(dto.getYy())
                .mnth(dto.getMnth())
                .weekStartDt(DateUtils.getWeekStartDateStr(dto.getStdrdDt()))
                .build();
    }

    /**
     * 팩토리 메서드 패턴
     *
     * @param dto {@link JrnlDreamDto}
     * @return {@link JrnlCacheEvictParam}
     */
    public static JrnlCacheEvictParam of(final JrnlDreamDto dto) throws Exception {
        return JrnlCacheEvictParam.builder()
                .regstrId(dto.getRegstrId())
                .id(dto.getId())
                .jrnlDayId(dto.getJrnlDayId())
                .yy(dto.getYy())
                .mnth(dto.getMnth())
                .weekStartDt(DateUtils.getWeekStartDateStr(dto.getStdrdDt()))
                .build();
    }

    /**
     * 팩토리 메서드 패턴
     *
     * @param dto {@link JrnlIntrptDto}
     * @return {@link JrnlCacheEvictParam}
     */
    public static JrnlCacheEvictParam of(final JrnlIntrptDto dto) throws Exception {
        return JrnlCacheEvictParam.builder()
                .regstrId(dto.getRegstrId())
                .id(dto.getId())
                .jrnlDayId(dto.getJrnlDayId())
                .jrnlDreamId(dto.getJrnlDreamId())
                .yy(dto.getYy())
                .mnth(dto.getMnth())
                .weekStartDt(DateUtils.getWeekStartDateStr(dto.getStdrdDt()))
                .build();
    }

    /**
     * 팩토리 메서드 패턴
     *
     * @param dto {@link JrnlTodoDto}
     * @return {@link JrnlCacheEvictParam}
     */
    public static JrnlCacheEvictParam of(final JrnlSumryDto dto) {
        return JrnlCacheEvictParam.builder()
                .regstrId(dto.getRegstrId())
                .id(dto.getId())
                .yy(dto.getYy())
                .build();
    }

    /**
     * 팩토리 메서드 패턴
     *
     * @param dto {@link JrnlTodoDto}
     * @return {@link JrnlCacheEvictParam}
     */
    public static JrnlCacheEvictParam of(final JrnlSumryReviewDto dto) {
        return JrnlCacheEvictParam.builder()
                .regstrId(dto.getRegstrId())
                .id(dto.getId())
                .jrnlSumryId(dto.getJrnlSumryId())
                .yy(dto.getYy())
                .build();
    }

    /**
     * 팩토리 메서드 패턴
     *
     * @param dto {@link JrnlTodoDto}
     * @return {@link JrnlCacheEvictParam}
     */
    public static JrnlCacheEvictParam of(final JrnlTodoDto dto) {
        return JrnlCacheEvictParam.builder()
                .regstrId(dto.getRegstrId())
                .id(dto.getId())
                .id(dto.getId())
                .yy(dto.getYy())
                .mnth(dto.getMnth())
                .build();
    }

}
