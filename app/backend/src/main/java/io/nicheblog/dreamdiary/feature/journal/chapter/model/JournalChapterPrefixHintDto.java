package io.nicheblog.dreamdiary.feature.journal.chapter.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * 챕터 Prefix 필터로 화면에서 숨겨진 말머리 힌트 DTO.
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString
public class JournalChapterPrefixHintDto {

    /** 말머리 ID */
    private Integer prefixId;
    /** 말머리명 */
    private String prefixName;
    /** 말머리 표시 색상 */
    private String prefixColor;
}
