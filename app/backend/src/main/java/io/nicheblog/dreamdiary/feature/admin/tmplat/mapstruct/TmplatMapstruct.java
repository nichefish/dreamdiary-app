package io.nicheblog.dreamdiary.feature.admin.tmplat.mapstruct;

import io.nicheblog.dreamdiary.feature.admin.tmplat.entity.TmplatEntity;
import io.nicheblog.dreamdiary.feature.admin.tmplat.model.TmplatDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * TmplatMapstruct
 * <pre>
 *  템플릿 Entity ↔ DTO 매핑 정의.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface TmplatMapstruct
        extends BaseWriteMapstruct<TmplatDto, TmplatEntity>, BaseReadMapstruct<TmplatDto, TmplatEntity> {

    TmplatMapstruct INSTANCE = Mappers.getMapper(TmplatMapstruct.class);

    @Override
    @Named("toDto")
    TmplatDto toDto(final TmplatEntity entity) throws Exception;

    @Override
    TmplatEntity toEntity(final TmplatDto dto) throws Exception;

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final TmplatDto dto, final @MappingTarget TmplatEntity entity) throws Exception;
}