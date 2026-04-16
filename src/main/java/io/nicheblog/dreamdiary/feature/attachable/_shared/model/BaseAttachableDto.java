package io.nicheblog.dreamdiary.feature.attachable._shared.model;

import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * BaseAttachableDto
 * <pre>
 *  (공통/상속) 분류 속성 Dto.
 *  "All classes in the hierarchy must be annotated with @SuperBuilder."
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class BaseAttachableDto
        extends BaseAuditDto {

    /** 글 번호 */
    @Positive
    protected Integer id;
    /** 컨텐츠 타입 */
    @Size(max = 50)
    protected String contentType;
    /** (수정시) 조치일자 변경하지 않음 여부 (Y/N) */
    @Builder.Default
    protected String managtDtUpdtYn = "N";
    /** 새 글 여부 */
    @Builder.Default
    protected Boolean isNew = false;

    /* ----- */

    /**
     * 복합키 객체 반환.
     * @return {@link BaseAttachableKey} -- 글 번호와 콘텐츠 유형을 포함하는 복합키 객체
     */
    public BaseAttachableKey getAttachableKey() {
        return new BaseAttachableKey(this.getId(), this.getContentType());
    }
}
