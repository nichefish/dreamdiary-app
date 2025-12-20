package io.nicheblog.dreamdiary.global.intrfc.model.tagify;

import lombok.*;

/**
 * BaseTagifyDataDto
 * <pre>
 *  Tagify JsonString - { data } 파싱 Dto.
 *  Dto for tagify jsonstring { data } parse.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseTagifyDataDto {

    /** ctgr */
    private String ctgr;
    /** ctgr */
    private String value;
}
