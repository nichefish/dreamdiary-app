package io.nicheblog.dreamdiary.domain.jrnl.entry.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * JrnlEntrySmpDto
 * <pre>
 *  저널 항목 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class JrnlEntrySmpDto {

    /** 저널 항목 번호 */
    private Integer postNo;
    /** 제목 */
    private String title;
    /** 순번 */
    private Integer idx;
}