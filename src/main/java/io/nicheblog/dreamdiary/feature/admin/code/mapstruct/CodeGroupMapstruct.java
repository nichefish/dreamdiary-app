package io.nicheblog.dreamdiary.feature.admin.code.mapstruct;

import io.nicheblog.dreamdiary.feature.admin.code.model.CodeGroupDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeGroupEntity;
import io.nicheblog.dreamdiary.infrastructure.code.utils.CdUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {CdUtils.class})
public interface CodeGroupMapstruct
        extends BaseWriteMapstruct<CodeGroupDto, CodeGroupEntity>, BaseReadMapstruct<CodeGroupDto, CodeGroupEntity> {

    CodeGroupMapstruct INSTANCE = Mappers.getMapper(CodeGroupMapstruct.class);

    @Override
    @Named("toDto")
    @Mapping(target = "clCtgrNm", expression = "java(CdUtils.getDtlCdNm(\"CL_CTGR_CD\", entity.getClCtgrCd()))")
    @Mapping(target = "dtlCdList", expression = "java(CodeItemMapstruct.INSTANCE.toDtoList(entity.getDtlCdList()))")
    CodeGroupDto toDto(final CodeGroupEntity entity) throws Exception;

    @Override
    CodeGroupEntity toEntity(final CodeGroupDto dto) throws Exception;

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final CodeGroupDto dto, final @MappingTarget CodeGroupEntity entity) throws Exception;
}
