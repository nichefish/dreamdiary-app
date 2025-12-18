package io.nicheblog.dreamdiary.extension.clsf.tag.entity.embed;

import io.nicheblog.dreamdiary.extension.clsf.tag.entity.TagContentEntity;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * TagEmbedModule
 * <pre>
 *   Tag 모듈 인터페이스
 * </pre>
 *
 * @author nichefish
 * @see MapstructHelper
 */
public interface TagEmbedModule {
    /** Getter */
    TagEmbed getTag();

    /** Setter */
    void setTag(TagEmbed embed);
    
    /** 태그 번호 목록 */
    default List<Integer> getTagNoList() {
        if (this.getTag() == null) return new ArrayList<>();

        final List<TagContentEntity> tagList = this.getTag().getList();
        if (CollectionUtils.isEmpty(tagList)) return new ArrayList<>();
        
        return tagList.stream()
                .map(TagContentEntity::getRefTagNo)
                .toList();        
    }
}
