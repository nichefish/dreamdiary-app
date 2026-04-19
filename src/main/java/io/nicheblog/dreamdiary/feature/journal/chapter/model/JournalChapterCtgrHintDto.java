package io.nicheblog.dreamdiary.feature.journal.chapter.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * JournalChapterCtgrHintDto
 * <pre>
 *  저널 챕터 카테고리 힌트 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString
public class JournalChapterCtgrHintDto {

    /** 카테고리 코드 */
    private String categoryCode;
    /** 카테고리명 */
    private String categoryName;
}
