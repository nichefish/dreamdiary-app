package io.nicheblog.dreamdiary.extension.clsf.tag.model;

import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * TagPropertyDto
 * <pre>
 *  태그 속성 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TagPropertyDto
        extends BaseCrudDto
        implements Identifiable<Integer> {

    /** 태그 속성 번호 (PK) */
    private Integer tagPropertyNo;

    /** 태그 번호 */
    private Integer tagNo;

    /** 참조 컨텐츠 타입 */
    private String contentType;

    /** CSS 클래스 */
    private String cssClass;

    /** CSS 스타일 */
    private String cssStyle;

    /* ----- */

    @Override
    public Integer getKey() {
        return this.getTagPropertyNo();
    }
}
