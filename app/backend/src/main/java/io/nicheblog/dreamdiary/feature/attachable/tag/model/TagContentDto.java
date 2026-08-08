package io.nicheblog.dreamdiary.feature.attachable.tag.model;

import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * TagContentDto
 * <pre>
 *  태그-컨텐츠 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TagContentDto
        extends BaseCrudDto
        implements Identifiable<Integer>, Comparable<TagContentDto> {

    /** 태그-컨텐츠 ID */
    @Positive
    private Integer id;

    /** 참조 태그 번호 */
    @Positive
    private Integer tagId;

    /** 참조 글 번호 */
    @Positive
    private Integer refId;

    /** 참조 컨텐츠 타입 */
    @Size(max = 50)
    private String refContentType;

    /** 태그 카테고리 */
    @Size(max = 50)
    private String ctgr;

    /** 태그 정보 */
    private TagDto tag;

    /** 태그 이름 */
    @Size(max = 50)
    private String name;

    /** 태그 프로필 본문 */
    private String profileContent;

    /* ----- */

    /**
     * 표시용 태그 이름을 해석한다. flat {@code name} 우선, nested {@code tag.name} fallback.
     *
     * @return 표시 이름. 없으면 null
     */
    public String resolveDisplayName() {
        if (StringUtils.isNotBlank(this.name)) return this.name;
        if (this.tag != null && StringUtils.isNotBlank(this.tag.getName())) {
            return this.tag.getName();
        }
        return null;
    }

    /**
     * 태그이름 오름차순 정렬
     *
     * @param compare - 비교할 객체
     * @return 양수: 현재 객체가 더 큼, 음수: 현재 객체가 더 작음, 0: 두 객체가 같음
     */
    @SneakyThrows
    @Override
    public int compareTo(final @NotNull TagContentDto compare) {
        final String thisName = StringUtils.defaultString(this.resolveDisplayName());
        final String otherName = StringUtils.defaultString(compare.resolveDisplayName());
        return thisName.compareTo(otherName);
    }

    @Override
    public Integer getKey() {
        return this.id;
    }
}
