package io.nicheblog.dreamdiary.extension.clsf.sectn.entity.embed;

import io.nicheblog.dreamdiary.extension.clsf.sectn.entity.SectnEntity;
import lombok.*;
import org.hibernate.annotations.*;
import org.springframework.util.CollectionUtils;

import javax.persistence.OrderBy;
import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * SectnEmbed
 * <pre>
 *   위임 :: 단락 관련 정보. (entity level)
 * </pre>
 *
 * @author nichefish
 */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectnEmbed
        implements Serializable {

    @PostLoad
    private void onLoad() {
        this.cnt = (CollectionUtils.isEmpty(this.list)) ? 0 : this.list.size();
        this.hasSectn = this.cnt > 0;
    }

    /** 단락 목록 */
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "ref_post_no", referencedColumnName = "post_no", insertable = false, updatable = false)),
            @JoinColumnOrFormula(column = @JoinColumn(name = "ref_content_type", referencedColumnName = "content_type", insertable = false, updatable = false)),
    })
    @Fetch(FetchMode.SELECT)
    @OrderBy("state.sortOrdr ASC")
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("단락 목록")
    private List<SectnEntity> list;

    /** 단락 수 */
    @Transient
    private Integer cnt;

    /** 단락 존재 여부 */
    @Transient
    private Boolean hasSectn;
}