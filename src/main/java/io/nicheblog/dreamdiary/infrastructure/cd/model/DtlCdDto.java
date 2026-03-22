package io.nicheblog.dreamdiary.infrastructure.cd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.infrastructure.cd.entity.DtlCdKey;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * DtlCd
 * <pre>
 *  상세 코드(dtlCd) Dto.
 *  ※상세 코드(dtl_cd) = 분류 코드 하위의 상세 코드. 분류 코드(cl_cd)에 N:1로 귀속된다.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DtlCdDto
        extends BaseAuditDto
        implements Identifiable<DtlCdKey> {

    /** 상세 코드 */
    private String dtlCd;
    /** 상세 코드이름 */
    private String dtlCdNm;

    /** 상세 코드설명 */
    private String dc;

    /** 분류 코드 */
    private String clCd;

    /** 시스템 보호 여부 (Y/N) */
    @Builder.Default
    private String protectedYn = "N";

    /** 사용 여부 (Y/N) */
    @Builder.Default
    private String useYn = "N";

    /* ----- */

    @Override
    public DtlCdKey getKey() {
        return new DtlCdKey(this.clCd, this.dtlCd);
    }
}
