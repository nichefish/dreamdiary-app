package io.nicheblog.dreamdiary.infrastructure.code.entity;

import lombok.*;

import java.io.Serializable;

/**
 * CodeItemKey
 * <pre>
 *  상세 코드 복합키 (clCd + dtlCd)
 * </pre>
 *
 * @author nichefish
 * @see CodeItemEntity
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CodeItemKey
        implements Serializable {

    /** 분류 코드 */
    private String clCd;

    /** 상세 코드 */
    private String dtlCd;
}
