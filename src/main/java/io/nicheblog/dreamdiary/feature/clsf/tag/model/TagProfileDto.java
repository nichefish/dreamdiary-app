package io.nicheblog.dreamdiary.feature.clsf.tag.model;

import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.type.TextClass;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * TagProfileDto
 * <pre>
 *  Tag profile DTO.
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
public class TagProfileDto
        extends BaseCrudDto
        implements Identifiable<Integer> {

    /** 태그 프로필 번호 (PK) */
    private Integer id;
    /** 태그 번호 */
    private Integer tagId;
    /** 참조 컨텐츠 타입 */
    private String contentType;
    /** 본문 */
    private String cn;

    /** 시각 의미 */
    @Builder.Default
    private TextClass textClass = TextClass.DEFAULT;

    /** 렌더링 시 적용할 text class */
    @Builder.Default
    private String textClassCd = TextClass.DEFAULT.getKey();

    /* ----- */

    @Override
    public Integer getKey() {
        return this.id;
    }

}
