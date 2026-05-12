package io.nicheblog.dreamdiary.feature.attachable.managt.model;

import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditRegDto;
import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * ManagtrDto
 * <pre>
 *  조치자 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ManagtrDto
        extends BaseAuditRegDto {

    /** 조치자 ID */
    @Positive
    private Integer id;

    /** 참조 글 번호 */
    @Positive
    private Integer refId;

    /** 참조 컨텐츠 타입 */
    @Size(max = 50)
    private String refContentType;

    /* ----- */

    /**
     * 생성자.
     *
     * @param refKey 글 번호와 컨텐츠 타입을 포함하는 참조 복합키 객체
     */
    public ManagtrDto(final BaseAttachableKey refKey) {
        this.refId = refKey.getId();
        this.refContentType = refKey.getContentType();
    }}
