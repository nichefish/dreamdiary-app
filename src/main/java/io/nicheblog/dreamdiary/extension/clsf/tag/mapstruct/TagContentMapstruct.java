package io.nicheblog.dreamdiary.extension.clsf.tag.mapstruct;

import io.nicheblog.dreamdiary.extension.clsf.tag.entity.TagContentEntity;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.TagContentDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * TagContentMapstruct
 * <pre>
 *  태그-컨텐츠 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, TagMapstruct.class})
public interface TagContentMapstruct
        extends BaseReadMapstruct<TagContentDto, TagContentEntity>, BaseWriteMapstruct<TagContentDto, TagContentEntity> {

    TagContentMapstruct INSTANCE = Mappers.getMapper(TagContentMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "tag", expression = "java(TagMapstruct.INSTANCE.toDto(entity.getTag()))")
    @Mapping(target = "tagNm", expression = "java(entity.getTag().getTagNm())")
    @Mapping(target = "ctgr", expression = "java(entity.getTag().getCtgr())")
    TagContentDto toDto(final TagContentEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Named("toEntity")
    TagContentEntity toEntity(final TagContentDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final TagContentDto dto, final @MappingTarget TagContentEntity entity) throws Exception;
}
