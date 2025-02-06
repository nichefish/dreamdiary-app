package io.nicheblog.dreamdiary.extension.tag.mapstruct;

import io.nicheblog.dreamdiary.extension.tag.entity.ContentTagEntity;
import io.nicheblog.dreamdiary.extension.tag.model.ContentTagDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseCrudMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * ContentTagMapstruct
 * <pre>
 *  컨텐츠-태그 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, TagMapstruct.class})
public interface ContentTagMapstruct
        extends BaseCrudMapstruct<ContentTagDto, ContentTagDto, ContentTagEntity> {

    ContentTagMapstruct INSTANCE = Mappers.getMapper(ContentTagMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toDto")
    @Mapping(target = "tag", expression = "java(TagMapstruct.INSTANCE.toDto(entity.getTag()))")
    ContentTagDto toDto(final ContentTagEntity entity) throws Exception;

    /**
     * Entity -> ListDto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return ListDto -- 변환된 ListDto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toListDto")
    @Mapping(target = "tag", expression = "java(TagMapstruct.INSTANCE.toDto(entity.getTag()))")
    ContentTagDto toListDto(final ContentTagEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toEntity")
    ContentTagEntity toEntity(final ContentTagDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 DTO 객체
     * @param entity 업데이트할 대상 엔티티 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final ContentTagDto dto, final @MappingTarget ContentTagEntity entity) throws Exception;
}
