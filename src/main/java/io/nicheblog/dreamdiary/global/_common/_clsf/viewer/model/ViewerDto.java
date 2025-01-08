package io.nicheblog.dreamdiary.global._common._clsf.viewer.model;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseAuditRegDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * ViewerDto
 * <pre>
 *  컨텐츠 열람자 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ViewerDto
        extends BaseAuditRegDto {

    /** 조치자 번호 (PK) */
    private Integer managtrNo;

    /** 참조 글 번호 */
    private Integer refPostNo;

    /** 참조 컨텐츠 타입 */
    private String refContentType;

    /* ----- */

    /**
     * 생성자.
     * @param refKey 글 번호와 컨텐츠 타입을 포함하는 참조 복합키 객체
     */
    public ViewerDto(final BaseClsfKey refKey) {
        this.refPostNo = refKey.getPostNo();
        this.refContentType = refKey.getContentType();
    }
}
