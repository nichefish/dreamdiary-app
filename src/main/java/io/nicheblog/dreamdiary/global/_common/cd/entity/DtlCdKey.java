package io.nicheblog.dreamdiary.global._common.cd.entity;

import lombok.*;

import java.io.Serializable;

/**
 * DtlCdKey
 * <pre>
 *  상세 코드 복합키 (clCd + dtlCd)
 * </pre>
 *
 * @author nichefish
 * @see DtlCdEntity
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DtlCdKey
        implements Serializable {

    /** 분류 코드 */
    private String clCd;

    /** 상세 코드 */
    private String dtlCd;
}
