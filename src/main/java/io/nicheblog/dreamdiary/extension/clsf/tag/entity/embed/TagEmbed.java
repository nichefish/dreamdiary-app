package io.nicheblog.dreamdiary.extension.clsf.tag.entity.embed;

import io.nicheblog.dreamdiary.extension.clsf.tag.entity.ContentTagEntity;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.cmpstn.TagCmpstn;
import lombok.*;
import org.hibernate.annotations.*;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * TagEmbed
 * <pre>
 *  위임 :: 태그 관련 정보. (entity level)
 * </pre>
 *
 * @author nichefish
 * @see TagEmbedModule
 */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagEmbed
        implements Serializable {

    /** 컨텐츠 태그 목록 */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "ref_post_no", referencedColumnName = "post_no", insertable = false, updatable = false)),
            @JoinColumnOrFormula(column = @JoinColumn(name = "ref_content_type", referencedColumnName = "content_type", insertable = false, updatable = false)),
    })
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 10)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("컨텐츠 태그 목록")
    private List<ContentTagEntity> list;

    /**
     * 컨텐츠 태그 문자열 목록
     * {@link TagCmpstn}에서 파싱된 문자열을 전달받는 데 사용한다.
     */
    @Transient
    private List<String> tagStrList;

    /** 컨텐츠 태그 문자열 (','로 구분) */
    @Transient
    private String tagListStr;

    /** 정렬된 태그 목록 캐시 */
    @Transient
    private List<ContentTagEntity> sortedListCache;

    /**
     * getter override (정렬된 태그 목록 캐시 및 반환)
     */
    public List<ContentTagEntity> getList() {
        if (CollectionUtils.isEmpty(list)) return list;
        if (!CollectionUtils.isEmpty(sortedListCache)) return sortedListCache;

        sortedListCache = list.stream()
            .sorted(Comparator.comparing(
                (ContentTagEntity ct) -> ct.getTag().getTagNm(),
                Comparator.nullsLast(String::compareTo)
            ))
            .toList();
        return sortedListCache;
    }

    /**
     * setter override (정렬된 태그 목록 캐시 초기화)
     */
    public void setList(final List<ContentTagEntity> list) {
        this.list = list;
        this.sortedListCache = null;
    }
}