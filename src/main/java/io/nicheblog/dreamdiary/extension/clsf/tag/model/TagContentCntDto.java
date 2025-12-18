package io.nicheblog.dreamdiary.extension.clsf.tag.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * TagContentCntDto
 * <pre>
 *  태그 번호 - 갯수 맵 Dto.
 * </pre>
 * 
 * @author nichefish 
 */
@Getter
@Setter
@AllArgsConstructor
public class TagContentCntDto {
    
    /** 태그 번호 */
    private Integer tagNo;

    /** 개수 */
    private Long count;
}
