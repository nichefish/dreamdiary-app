package io.nicheblog.dreamdiary.extension.cache.model;

import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.domain.jrnl.dream.model.JrnlDreamDto;
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
    /** 글 번호 */
    private Integer postNo;
    /** 저널 일자 번호 */
    private Integer jrnlDayNo;
    /** 년도 */
    private Integer yy;
    /** 월 */
    private Integer mnth;

    /**
     * 팩토리 메서드 패턴
     *
     * @param dto {@link JrnlDayDto}
     * @return {@link JrnlCacheEvictParam}
     */
    public static JrnlCacheEvictParam of(final JrnlDayDto dto) {
        return JrnlCacheEvictParam.builder()
                .postNo(dto.getPostNo())
                .yy(dto.getYy())
                .mnth(dto.getMnth())
                .build();
    }

    /**
     * 팩토리 메서드 패턴
     *
     * @param dto {@link JrnlDiaryDto}
     * @return {@link JrnlCacheEvictParam}
     */
    public static JrnlCacheEvictParam of(final JrnlDiaryDto dto) {
        return JrnlCacheEvictParam.builder()
                .postNo(dto.getPostNo())
                .jrnlDayNo(dto.getJrnlDayNo())
                .yy(dto.getYy())
                .mnth(dto.getMnth())
                .build();
    }

    /**
     * 팩토리 메서드 패턴
     *
     * @param dto {@link JrnlDreamDto}
     * @return {@link JrnlCacheEvictParam}
     */
    public static JrnlCacheEvictParam of(final JrnlDreamDto dto) {
        return JrnlCacheEvictParam.builder()
                .postNo(dto.getPostNo())
                .jrnlDayNo(dto.getJrnlDayNo())
                .yy(dto.getYy())
                .mnth(dto.getMnth())
                .build();
    }
}
