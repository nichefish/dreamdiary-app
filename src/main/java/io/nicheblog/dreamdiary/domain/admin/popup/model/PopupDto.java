package io.nicheblog.dreamdiary.domain.admin.popup.model;

import io.nicheblog.dreamdiary.extension.clsf.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.extension.clsf.state.model.cmpstn.StateCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseClsfDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.intrfc.model.cmpstn.AtchFileCmpstn;
import io.nicheblog.dreamdiary.global.intrfc.model.cmpstn.AtchFileCmpstnModule;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * PopupDto
 * <pre>
 *  팝업 정보 관리 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PopupDto
        extends BaseClsfDto
        implements Identifiable<Integer>, AtchFileCmpstnModule, StateCmpstnModule {

    /** 팝업 코드 */
    @Size(max = 50)
    private String popupCd;

    /** 팝업 이름 */
    private String popupNm;

    /** 가로 */
    @Positive
    private Integer width;

    /** 세로 */
    @Positive
    private Integer height;

    /** 게시시작일시 */
    private String popupStartDt;

    /** 게시종료일시 */
    private String popupEndDt;

    /* ----- */

    @Override
    public Integer getKey() {
        return null;
    }

    /** 위임 :: 첨부파일 모듈 */
    public AtchFileCmpstn file;
    /** 위임 :: 상태 관리 모듈 */
    public StateCmpstn state;
}
