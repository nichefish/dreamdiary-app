package io.nicheblog.dreamdiary.global.intrfc.model.tagify;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * BaseTagifyDto
 * <pre>
 *  Tagify JsonString 파싱 Dto.
 *  Dto for tagify jsonstring parse.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseTagifyDto {

    /** value */
    private String value;

    /** data */
    private BaseTagifyDataDto data;
}
