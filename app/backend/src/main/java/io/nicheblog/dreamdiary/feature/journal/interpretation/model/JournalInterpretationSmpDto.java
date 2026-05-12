package io.nicheblog.dreamdiary.feature.journal.interpretation.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * JournalInterpretationSmpDto
 * <pre>
 *  저널 해석 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class JournalInterpretationSmpDto {

    /** 저널 해석 번호 */
    private Integer id;
    /** 제목 */
    private String title;
    /** 순번 */
    private Integer sortOrder;
}
