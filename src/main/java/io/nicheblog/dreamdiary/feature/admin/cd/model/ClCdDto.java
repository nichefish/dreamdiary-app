package io.nicheblog.dreamdiary.feature.admin.cd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditDto;
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
        implements Identifiable<String> {

    /** 목록 순번 */
    private Long rnum;

    /** 분류 코드 */
    private String clCd;
    /** 분류 코드이름 */
    private String clCdNm;

    /** 설명 */
    private String dc;

    /** 시스템 보호 여부 (Y/N) */
    @Builder.Default
    private String protectedYn = "N";

    /** 분류 코드 분류 코드 */
    private String clCtgrCd;
    /** 분류 코드 분류 코드명 */
    private String clCtgrNm;

    /** 상세 코드 목록 */
    List<DtlCdDto> dtlCdList;

    /** 사용 여부 (Y/N) */
    @Builder.Default
    private String useYn = "N";

    /** 상세 코드 개수 */
    @Builder.Default
    private Integer dtlCdCnt = 0;

    /** 등록 여부 */
    @Builder.Default
    private String regYn = "N";

    /* ----- */

    @Override
    public String getKey() {
        return this.clCd;
    }
}
