package io.nicheblog.dreamdiary.feature.journal.chapter.model;

import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
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
    /** 일반 챕터가 선택한 개인 말머리(0..1) */
    private PrefixDto prefix;
    /** 선택한 개인 말머리 ID */
    private Integer prefixId;
    /** 시스템 요약 챕터 여부 */
    private String summaryYn;

    /** 챕터 타입 (DIARY | DREAM | …) */
    private ChapterType chapterType;
}
