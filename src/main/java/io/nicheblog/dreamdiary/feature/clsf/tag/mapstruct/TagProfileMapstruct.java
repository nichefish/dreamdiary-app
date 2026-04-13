package io.nicheblog.dreamdiary.feature.clsf.tag.mapstruct;

import io.nicheblog.dreamdiary.feature.clsf.tag.entity.TagProfileEntity;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagProfileDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * TagProfileMapstruct
 * <pre>
 *  태그 프로필(해석) MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {})
public interface TagProfileMapstruct
        extends BaseWriteMapstruct<TagProfileDto, TagProfileEntity>, BaseReadMapstruct<TagProfileDto, TagProfileEntity> {

    TagProfileMapstruct INSTANCE = Mappers.getMapper(TagProfileMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    TagProfileDto toDto(final TagProfileEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    TagProfileEntity toEntity(final TagProfileDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    void updateFromDto(final TagProfileDto dto, final @MappingTarget TagProfileEntity entity) throws Exception;
}
