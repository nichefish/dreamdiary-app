package io.nicheblog.dreamdiary.domain.jrnl.dream.mapstruct;

import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamTagEntity;
import io.nicheblog.dreamdiary.extension.clsf.tag.mapstruct.ContentTagMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseCrudMapstruct;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * JrnlDreamTagMapstruct
 * <pre>
 *  저널 꿈 태그 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {ContentTagMapstruct.class})
public interface JrnlDreamTagMapstruct
        extends BaseCrudMapstruct<TagDto, TagDto, JrnlDreamTagEntity> {

    JrnlDreamTagMapstruct INSTANCE = Mappers.getMapper(JrnlDreamTagMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toDto")
    TagDto toDto(final JrnlDreamTagEntity entity) throws Exception;

    /**
     * Entity -> ListDto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return ListDto -- 변환된 ListDto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toListDto")
    TagDto toListDto(final JrnlDreamTagEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    JrnlDreamTagEntity toEntity(final TagDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final TagDto dto, final @MappingTarget JrnlDreamTagEntity entity) throws Exception;
}
