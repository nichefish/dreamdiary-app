package io.nicheblog.dreamdiary.domain.admin.tmplat.model;

import io.nicheblog.dreamdiary.extension.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.extension.state.model.cmpstn.StateCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * TmplatDefDto
 * <pre>
 *  템플릿 정의 정보 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class TmplatDefDto
        extends BaseAuditDto
        implements Identifiable<Integer>, StateCmpstnModule {

    /** 고유 ID (PK) */
    @Positive
    private Integer tmplatDefNo;

    /** 템플릿 정의 코드 */
    @Size(max = 50)
    private String tmplatDefCd;

    /** 제목 */
    private String title;

    /* ----- */

    @Override
    public Integer getKey() {
        return this.tmplatDefNo;
    }

    /** 위임 :: 상태 관리 모듈 */
    public StateCmpstn state;
}
