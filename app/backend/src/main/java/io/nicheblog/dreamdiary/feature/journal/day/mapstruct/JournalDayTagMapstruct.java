package io.nicheblog.dreamdiary.feature.journal.day.mapstruct;

import io.nicheblog.dreamdiary.feature.attachable.tag.mapstruct.TagContentMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayTagEntity;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * JournalDayTagMapstruct
 * <pre>
 *  저널 일자 태그 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {TagContentMapstruct.class})
public interface JournalDayTagMapstruct
        extends BaseReadMapstruct<TagDto, JournalDayTagEntity>, BaseWriteMapstruct<TagDto, JournalDayTagEntity> {

    JournalDayTagMapstruct INSTANCE = Mappers.getMapper(JournalDayTagMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "ctgr", expression = "java(entity.getCtgr())")
    TagDto toDto(final JournalDayTagEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    JournalDayTagEntity toEntity(final TagDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final TagDto dto, final @MappingTarget JournalDayTagEntity entity) throws Exception;
}
