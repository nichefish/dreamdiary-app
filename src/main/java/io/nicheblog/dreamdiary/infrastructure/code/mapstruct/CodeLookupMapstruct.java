package io.nicheblog.dreamdiary.infrastructure.code.mapstruct;

import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeItemEntity;
import io.nicheblog.dreamdiary.infrastructure.code.model.CodeLookupItem;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * CodeLookupMapstruct.
 * <pre>
 *  코드 lookup 전용 매퍼.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CodeLookupMapstruct {

    /**
     * Entity -> LookupItem 변환.
     *
     * @param entity 상세코드 Entity.
     * @return 조회용 모델.
     */
    CodeLookupItem toLookupItem(final CodeItemEntity entity);
}
