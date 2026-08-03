package io.nicheblog.dreamdiary.feature.admin.code.mapstruct;

import io.nicheblog.dreamdiary.feature.admin.code.model.CodeGroupDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeGroupEntity;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface CodeGroupMapstruct
        extends BaseWriteMapstruct<CodeGroupDto, CodeGroupEntity>, BaseReadMapstruct<CodeGroupDto, CodeGroupEntity> {

    CodeGroupMapstruct INSTANCE = Mappers.getMapper(CodeGroupMapstruct.class);

    @Override
    @Named("toDto")
    @Mapping(target = "codeItems", expression = "java(CodeItemMapstruct.INSTANCE.toDtoList(entity.getCodeItems()))")
    CodeGroupDto toDto(final CodeGroupEntity entity) throws Exception;

    @Override
    CodeGroupEntity toEntity(final CodeGroupDto dto) throws Exception;

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final CodeGroupDto dto, final @MappingTarget CodeGroupEntity entity) throws Exception;
}
