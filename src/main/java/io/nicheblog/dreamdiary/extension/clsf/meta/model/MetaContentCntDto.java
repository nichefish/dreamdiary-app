package io.nicheblog.dreamdiary.extension.clsf.meta.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * MetaContentCntDto
 * <pre>
 *  메타 번호 - 갯수 맵 Dto.
 * </pre>
 * 
 * @author nichefish 
 */
@Getter
@Setter
@AllArgsConstructor
public class MetaContentCntDto {
    
    /** 메타 번호 */
    private Integer metaNo;

    /** 개수 */
    private Long count;
}
