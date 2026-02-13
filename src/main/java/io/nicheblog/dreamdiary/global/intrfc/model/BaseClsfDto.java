package io.nicheblog.dreamdiary.global.intrfc.model;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * BaseClsfDto
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
public class BaseClsfDto
        extends BaseAuditDto {

    /** 글 번호 */
    @Positive
    protected Integer postNo;
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
     * @return {@link BaseClsfKey} -- 글 번호와 콘텐츠 유형을 포함하는 복합키 객체
     */
    public BaseClsfKey getClsfKey() {
        return new BaseClsfKey(this.getPostNo(), this.getContentType());
    }
}
