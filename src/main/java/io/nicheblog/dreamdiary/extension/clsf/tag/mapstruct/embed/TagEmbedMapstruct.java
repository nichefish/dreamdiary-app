package io.nicheblog.dreamdiary.extension.clsf.tag.mapstruct.embed;

import io.nicheblog.dreamdiary.extension.clsf.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.extension.clsf.tag.mapstruct.TagContentMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.tag.mapstruct.TagMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * TagEmbedMapstruct
 * <pre>
 *  태그 모듈 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, TagMapstruct.class, TagContentMapstruct.class}, builder = @Builder(disableBuilder = true))
public interface TagEmbedMapstruct
        extends BaseWriteMapstruct<TagCmpstn, TagEmbed>, BaseMapstruct<TagCmpstn, TagEmbed> {

    TagEmbedMapstruct INSTANCE = Mappers.getMapper(TagEmbedMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Mapping(target = "list", expression = "java(TagContentMapstruct.INSTANCE.toDtoList(entity.getList()))")
    TagCmpstn toDto(final TagEmbed entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Mapping(target = "list", expression = "java(TagContentMapstruct.INSTANCE.toEntityList(dto.getList()))")
    @Mapping(target = "tagStrList", expression = "java(dto.getParsedTagStrList())")
    TagEmbed toEntity(final TagCmpstn dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final TagCmpstn dto, final @MappingTarget TagEmbed entity) throws Exception;
}
