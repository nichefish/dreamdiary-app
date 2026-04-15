package io.nicheblog.dreamdiary.infrastructure.code.mapstruct;

import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeItemEntity;
import io.nicheblog.dreamdiary.infrastructure.code.model.CdLookupItem;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * CdLookupMapstruct.
 * <pre>
 *  코드 lookup 전용 매퍼.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CdLookupMapstruct {

    /**
     * Entity -> LookupItem 변환.
     *
     * @param entity 상세코드 Entity.
     * @return 조회용 모델.
     */
    CdLookupItem toLookupItem(final CodeItemEntity entity);
}
