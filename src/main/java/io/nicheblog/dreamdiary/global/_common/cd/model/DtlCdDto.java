package io.nicheblog.dreamdiary.global._common.cd.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.nicheblog.dreamdiary.global._common._clsf.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.global._common._clsf.state.model.cmpstn.StateCmpstnModule;
import io.nicheblog.dreamdiary.global._common.cd.entity.DtlCdKey;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
        implements StateCmpstnModule, Identifiable<DtlCdKey> {

    /** 상세 코드 */
    private String dtlCd;

    /** 상세 코드이름 */
    private String dtlCdNm;

    /** 상세 코드설명 */
    private String dc;

    /** 분류 코드 */
    private String clCd;

    /* ----- */

    @Override
    public DtlCdKey getKey() {
        return new DtlCdKey(this.clCd, this.dtlCd);
    }

    /** 위임 :: 상태 관리 모듈 */
    public StateCmpstn state;
}
