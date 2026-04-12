package io.nicheblog.dreamdiary.feature.clsf.tag.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * TagContentCntDto
 * <pre>
 *  태그 ID - 개수 매핑 DTO.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@AllArgsConstructor
public class TagContentCntDto {

    /** 태그 ID */
    private Integer tagId;
    /** 개수 */
    private Long count;

}
