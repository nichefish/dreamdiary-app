package io.nicheblog.dreamdiary.feature.journal.chapter.model;

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
}
