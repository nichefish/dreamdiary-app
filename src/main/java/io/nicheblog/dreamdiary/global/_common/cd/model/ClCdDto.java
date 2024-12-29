package io.nicheblog.dreamdiary.global._common.cd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.nicheblog.dreamdiary.global._common._clsf.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.global._common._clsf.state.model.cmpstn.StateCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * ClCdDto
 * <pre>
 *  분류 코드(clCd) Dto.
 *  ※분류 코드(cl_cd) = 상위 분류 코드. 상세 코드(dtl_cd)를 1:N 묶음으로 관리한다.
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
public class ClCdDto
        extends BaseAuditDto
        implements StateCmpstnModule, Identifiable<String> {

    /** 목록 순번 */
    private Long rnum;

    /** 분류 코드 */
    private String clCd;

    /** 분류 코드이름 */
    private String clCdNm;

    /** 설명 */
    private String dc;

    /** 분류 코드 분류 코드 */
    private String clCtgrCd;

    /** 분류 코드 분류 코드명 */
    private String clCtgrNm;

    /** 상세 코드 목록 */
    List<DtlCdDto> dtlCdList;

    /** 상세 코드 개수 */
    @Builder.Default
    private Integer dtlCdCnt = 0;

    /* ----- */

    @Override
    public String getKey() {
        return this.clCd;
    }

    /** 위임 :: 상태 관리 모듈 */
    public StateCmpstn state;
}
