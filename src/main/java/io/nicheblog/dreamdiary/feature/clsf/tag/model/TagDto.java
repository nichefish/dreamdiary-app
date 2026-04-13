package io.nicheblog.dreamdiary.feature.clsf.tag.model;

import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.type.TextClass;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * TagDto
 * <pre>
 *  태그 DTO.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = { "ctgr", "tagNm" }, callSuper = false)
public class TagDto
        extends BaseCrudDto
        implements Identifiable<Integer>, Comparable<TagDto> {

    /** 태그 ID */
    @Positive
    private Integer id;

    /** 태그 카테고리 ID */
    private Integer tagCategoryId;

    /** 태그 카테고리 */
    @Builder.Default
    @Size(max = 50)
    private String ctgr = "";

    /** 태그 이름 */
    @Size(max = 50)
    private String tagNm;

    /** 태그-컨텐츠 목록 */
    private List<TagContentDto> tagContentList;

    /** 게시물 목록 */
    private List<?> contentList;

    /** 태그 크기 (=컨텐츠 개수) */
    @Builder.Default
    private Integer contentSize = 0;

    /** 태그 CSS 클래스 */
    private String tagClass;

    /** 태그 시각 의미 */
    @Builder.Default
    private TextClass textSemantic = TextClass.DEFAULT;

    /** 렌더링용 text class 코드 */
    @Builder.Default
    private String textClassCd = TextClass.DEFAULT.getKey();

    /** Bootstrap text class */
    @Builder.Default
    private String textClass = "";

    public TagDto(final String tagNm) {
        this.tagNm = tagNm;
        this.ctgr = "";
    }

    /**
     * 생성자.
     *
     * @param tagNm - 생성할 태그의 이름
     * @param ctgr - 생성할 태그의 카테고리
     */
    public TagDto(final String tagNm, final String ctgr) {
        this.tagNm = tagNm;
        this.ctgr = StringUtils.isEmpty(ctgr) ? "" : ctgr;
    }

    /**
     * 생성자.

     * @param tagNm - 생성할 태그 번호
     * @param ctgr - 생성할 태그의 카테고리
     */
    public TagDto(final Integer tagId, final String tagNm, final String ctgr) {
        this(tagNm, ctgr);
        this.id = tagId;
    }

    /**
     * 태그이름 오름차순 정렬
     *
     * @param other - 비교할 `TagDto` 객체
     * @return int - 사전적으로 앞서면 음수, 같으면 0, 뒤에 있으면 양수를 반환
     */
    @SneakyThrows
    @Override
    public int compareTo(final @NotNull TagDto other) {
        return this.getTagNm().compareTo(other.getTagNm());
    }

    @Override
    public Integer getKey() {
        return this.id;
    }

}
