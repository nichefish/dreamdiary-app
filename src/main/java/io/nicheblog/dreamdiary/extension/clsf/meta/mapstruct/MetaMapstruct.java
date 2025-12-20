package io.nicheblog.dreamdiary.extension.clsf.meta.mapstruct;

import io.nicheblog.dreamdiary.extension.clsf.meta.entity.MetaEntity;
import io.nicheblog.dreamdiary.extension.clsf.meta.entity.MetaSmpEntity;
import io.nicheblog.dreamdiary.extension.clsf.meta.model.MetaDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * MetaMapstruct
 * <pre>
 *  메타 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {})
public interface MetaMapstruct
        extends BaseWriteMapstruct<MetaDto, MetaEntity>, BaseReadMapstruct<MetaDto, MetaEntity> {

    MetaMapstruct INSTANCE = Mappers.getMapper(MetaMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "metaContentList", expression = "java(MetaContentMapstruct.INSTANCE.toDtoList(entity.getMetaContentList()))")
    MetaDto toDto(final MetaEntity entity) throws Exception;

    /**
     * SmpEntity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    MetaDto toDto(final MetaSmpEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    MetaEntity toEntity(final MetaDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final MetaDto dto, final @MappingTarget MetaEntity entity) throws Exception;
}
