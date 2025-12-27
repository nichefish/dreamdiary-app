package io.nicheblog.dreamdiary.extension.clsf.meta.mapstruct;

import io.nicheblog.dreamdiary.extension.clsf.meta.entity.MetaContentEntity;
import io.nicheblog.dreamdiary.extension.clsf.meta.model.MetaContentDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * MetaContentMapstruct
 * <pre>
 *  메타-컨텐츠 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, MetaMapstruct.class})
public interface MetaContentMapstruct
        extends BaseReadMapstruct<MetaContentDto, MetaContentEntity>, BaseWriteMapstruct<MetaContentDto, MetaContentEntity> {

    MetaContentMapstruct INSTANCE = Mappers.getMapper(MetaContentMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "meta", expression = "java(MetaMapstruct.INSTANCE.toDto(entity.getMeta()))")
    @Mapping(target = "metaNm", expression = "java(entity.getMeta().getMetaNm())")
    @Mapping(target = "ctgr", expression = "java(entity.getMeta().getCtgr())")
    @Mapping(target = "label", expression = "java(entity.getMeta().getLabel())")
    MetaContentDto toDto(final MetaContentEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Named("toEntity")
    MetaContentEntity toEntity(final MetaContentDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final MetaContentDto dto, final @MappingTarget MetaContentEntity entity) throws Exception;
}
