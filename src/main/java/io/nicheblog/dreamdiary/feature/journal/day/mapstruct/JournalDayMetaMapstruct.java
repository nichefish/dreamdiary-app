package io.nicheblog.dreamdiary.feature.journal.day.mapstruct;

import io.nicheblog.dreamdiary.feature.clsf.meta.mapstruct.MetaContentMapstruct;
import io.nicheblog.dreamdiary.feature.clsf.meta.model.MetaDto;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayMetaEntity;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * JournalDayMetaMapstruct
 * <pre>
 *  저널 일자 메타 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {MetaContentMapstruct.class})
public interface JournalDayMetaMapstruct
        extends BaseReadMapstruct<MetaDto, JournalDayMetaEntity>, BaseWriteMapstruct<MetaDto, JournalDayMetaEntity> {

    JournalDayMetaMapstruct INSTANCE = Mappers.getMapper(JournalDayMetaMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    MetaDto toDto(final JournalDayMetaEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    JournalDayMetaEntity toEntity(final MetaDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final MetaDto dto, final @MappingTarget JournalDayMetaEntity entity) throws Exception;
}
