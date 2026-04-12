package io.nicheblog.dreamdiary.feature.jrnl.chapter.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * JrnlChapterCtgrHintDto
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
public class JrnlChapterCtgrHintDto {

    /** 카테고리 코드 */
    private String ctgrCd;
    /** 카테고리명 */
    private String ctgrNm;
}
