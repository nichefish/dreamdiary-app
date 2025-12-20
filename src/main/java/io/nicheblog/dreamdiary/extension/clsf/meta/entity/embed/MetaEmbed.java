package io.nicheblog.dreamdiary.extension.clsf.meta.entity.embed;

import io.nicheblog.dreamdiary.extension.clsf.meta.entity.MetaContentEntity;
import io.nicheblog.dreamdiary.extension.clsf.meta.model.cmpstn.MetaCmpstn;
import lombok.*;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.annotations.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * MetaEmbed
 * <pre>
 *  위임 :: 메타 관련 정보. (entity level)
 * </pre>
 *
 * @author nichefish
 * @see MetaEmbedModule
 */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaEmbed
        implements Serializable {

    /** 메타-컨텐츠 목록 */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "ref_post_no", referencedColumnName = "post_no", insertable = false, updatable = false)),
            @JoinColumnOrFormula(column = @JoinColumn(name = "ref_content_type", referencedColumnName = "content_type", insertable = false, updatable = false)),
    })
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 10)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("메타-컨텐츠 목록")
    private List<MetaContentEntity> list;

    /**
     * 메타-컨텐츠 문자열 목록
     * {@link MetaCmpstn}에서 파싱된 문자열을 전달받는 데 사용한다.
     */
    @Transient
    private List<String> metaStrList;

    /** 메타-컨텐츠 문자열 (','로 구분) */
    @Transient
    private String metaListStr;

    /** 정렬된 메타 목록 캐시 */
    @Transient
    private List<MetaContentEntity> sortedListCache;

    /**
     * getter override (정렬된 메타 목록 캐시 및 반환)
     */
    public List<MetaContentEntity> getList() {
        if (CollectionUtils.isEmpty(list)) return list;
        if (!CollectionUtils.isEmpty(sortedListCache)) return sortedListCache;

        sortedListCache = list.stream()
            .sorted(Comparator.comparing(
                (MetaContentEntity ct) -> ct.getMeta().getMetaNm(),
                Comparator.nullsLast(String::compareTo)
            ))
            .toList();
        return sortedListCache;
    }

    /**
     * setter override (정렬된 메타 목록 캐시 초기화)
     */
    public void setList(final List<MetaContentEntity> list) {
        this.list = list;
        this.sortedListCache = null;
    }
}