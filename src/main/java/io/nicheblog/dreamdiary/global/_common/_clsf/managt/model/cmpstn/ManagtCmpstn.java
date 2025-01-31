package io.nicheblog.dreamdiary.global._common._clsf.managt.model.cmpstn;

import io.nicheblog.dreamdiary.auth.model.AuditorDto;
import io.nicheblog.dreamdiary.global._common._clsf.managt.model.ManagtrDto;
import lombok.*;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ManagtCmpstn
 * <pre>
 *  위임 :: 조치 관련 정보. (dto level)
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagtCmpstn
        implements Serializable {

    /** 조치자(작업자)ID */
    private String managtrId;

    /** 조치자(작업자)이름 */
    private String managtrNm;

    /** 조치자 정보 */
    private AuditorDto managtrInfo;

    /** 처리(조치)자 여부 */
    private Boolean isManagtr;

    /** 조치(작업)일시 */
    private String managtDt;

    /** (수정시) 조치일자 변경하지 않음 여부 (Y/N) */
    @Builder.Default
    @Size(min = 1, max = 1)
    @Pattern(regexp = "^[YN]$")
    private String managtDtUpdtYn = "N";

    /** 게시물 조치자 목록 */
    private List<ManagtrDto> list;

    /* ----- */

    /**
     * 게시물 조치자 목록 추가
     *
     * @param managtr 추가할 조치자 정보
     */
    public void addManagtr(final ManagtrDto managtr) {
        if (this.list == null) this.list = new ArrayList<>();
        list.add(managtr);
    }
}