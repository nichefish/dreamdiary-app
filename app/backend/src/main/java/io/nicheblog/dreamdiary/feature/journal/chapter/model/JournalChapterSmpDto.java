package io.nicheblog.dreamdiary.feature.journal.chapter.model;

import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * JournalChapterSmpDto
 * <pre>
 *  저널 챕터 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class JournalChapterSmpDto {

    /** 저널 챕터 번호 */
    private Integer id;
    /** 제목 */
    private String title;
    /** 순번 */
    private Integer sortOrder;
    /** 카테고리 코드 */
    private String categoryCode;
    /** 카테고리 이름 */
    private String categoryName;

    /** 챕터 타입 (DIARY | DREAM | …) */
    private ChapterType chapterType;
}
