package io.nicheblog.dreamdiary.feature.clsf.meta.entity.embed;

import io.nicheblog.dreamdiary.feature.clsf.meta.entity.MetaContentEntity;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * MetaEmbedModule
 * <pre>
 *   Meta 모듈 인터페이스
 * </pre>
 *
 * @author nichefish
 * @see MapstructHelper
 */
public interface MetaEmbedModule {
    /** Getter */
    MetaEmbed getMeta();

    /** Setter */
    void setMeta(MetaEmbed embed);
    
    /** 메타 ID 목록 */
    default List<Integer> getMetaIdList() {
        if (this.getMeta() == null) return new ArrayList<>();

        final List<MetaContentEntity> metaList = this.getMeta().getList();
        if (CollectionUtils.isEmpty(metaList)) return new ArrayList<>();
        
        return metaList.stream()
                .map(MetaContentEntity::getMetaId)
                .toList();        
    }
}
