package io.nicheblog.dreamdiary.extension.clsf.tag.model;

import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;
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

    /** 태그-컨텐츠 번호 (PK) */
    @Positive
    private Integer tagContentNo;

    /** 참조 태그 번호 */
    @Positive
    private Integer refTagNo;

    /** 참조 글 번호 */
    @Positive
    private Integer refPostNo;

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
    private String tagNm;

    /* ----- */

    /**
     * 태그이름 오름차순 정렬
     *
     * @param compare - 비교할 객체
     * @return 양수: 현재 객체가 더 큼, 음수: 현재 객체가 더 작음, 0: 두 객체가 같음
     */
    @SneakyThrows
    @Override
    public int compareTo(final @NotNull TagContentDto compare) {
        String thisTagNm = this.getTagNm();
        String otherTagNm = compare.getTagNm();
        return thisTagNm.compareTo(otherTagNm);
    }

    @Override
    public Integer getKey() {
        return this.tagContentNo;
    }
}
