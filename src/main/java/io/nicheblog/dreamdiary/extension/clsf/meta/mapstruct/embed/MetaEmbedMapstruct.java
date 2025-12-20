package io.nicheblog.dreamdiary.extension.clsf.meta.mapstruct.embed;

import io.nicheblog.dreamdiary.extension.clsf.meta.entity.embed.MetaEmbed;
import io.nicheblog.dreamdiary.extension.clsf.meta.mapstruct.MetaContentMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.meta.mapstruct.MetaMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.meta.model.cmpstn.MetaCmpstn;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * MetaEmbedMapstruct
 * <pre>
 *  메타 모듈 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, MetaMapstruct.class, MetaContentMapstruct.class}, builder = @Builder(disableBuilder = true))
public interface MetaEmbedMapstruct
        extends BaseWriteMapstruct<MetaCmpstn, MetaEmbed>, BaseMapstruct<MetaCmpstn, MetaEmbed> {

    MetaEmbedMapstruct INSTANCE = Mappers.getMapper(MetaEmbedMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Mapping(target = "list", expression = "java(MetaContentMapstruct.INSTANCE.toDtoList(entity.getList()))")
    MetaCmpstn toDto(final MetaEmbed entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Mapping(target = "list", expression = "java(MetaContentMapstruct.INSTANCE.toEntityList(dto.getList()))")
    @Mapping(target = "metaStrList", expression = "java(dto.getParsedMetaStrList())")
    MetaEmbed toEntity(final MetaCmpstn dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final MetaCmpstn dto, final @MappingTarget MetaEmbed entity) throws Exception;
}
