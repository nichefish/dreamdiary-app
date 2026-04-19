package io.nicheblog.dreamdiary.feature.admin.code.mapstruct;

import io.nicheblog.dreamdiary.feature.admin.code.model.CodeItemDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeItemEntity;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CodeItemMapstruct
        extends BaseReadMapstruct<CodeItemDto, CodeItemEntity>, BaseWriteMapstruct<CodeItemDto, CodeItemEntity> {

    CodeItemMapstruct INSTANCE = Mappers.getMapper(CodeItemMapstruct.class);

    @Override
    @Named("toDto")
    CodeItemDto toDto(final CodeItemEntity entity) throws Exception;

    @Override
    CodeItemEntity toEntity(final CodeItemDto dto) throws Exception;

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final CodeItemDto dto, final @MappingTarget CodeItemEntity entity) throws Exception;
}
