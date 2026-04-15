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

    /** 태그 프로필 ID */
    private Integer id;
    /** 태그 ID */
    private Integer tagId;
    /** 태그 카테고리명 */
    private String ctgr;
    /** 태그 카테고리 ID */
    private Integer tagCategoryId;
    /** 참조 컨텐츠 타입 */
    private String contentType;
    /** 본문 */
    private String content;

    /** 개별 태그 시각 의미 ({@code null} = 카테고리/기본 색 상속) */
    private TextClass textClass;

    /** 개별 렌더링용 text class 코드 ({@code null} 또는 빈 문자열 = 상속) */
    private String textClassCd;

    /** 카테고리 프로필 ID */
    private Integer categoryProfileId;

    /** 카테고리 시각 의미 */
    @Builder.Default
    private TextClass categoryTextClass = TextClass.DEFAULT;

    /** 카테고리 렌더링 시 적용할 text class */
    @Builder.Default
    private String categoryTextClassCd = TextClass.DEFAULT.getKey();

    /* ----- */

    @Override
    public Integer getKey() {
        return this.id;
    }

}
