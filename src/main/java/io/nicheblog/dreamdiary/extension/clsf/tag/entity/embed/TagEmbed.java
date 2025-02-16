package io.nicheblog.dreamdiary.extension.clsf.tag.entity.embed;

import io.nicheblog.dreamdiary.extension.clsf.tag.entity.ContentTagEntity;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.cmpstn.TagCmpstn;
import lombok.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import java.io.Serializable;
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
}