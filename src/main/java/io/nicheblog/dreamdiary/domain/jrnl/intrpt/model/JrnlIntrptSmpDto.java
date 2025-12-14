package io.nicheblog.dreamdiary.domain.jrnl.intrpt.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * JrnlIntrptSmpDto
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
public class JrnlIntrptSmpDto {

    /** 저널 항목 번호 */
    private Integer postNo;
    /** 제목 */
    private String title;
    /** 순번 */
    private Integer idx;
}