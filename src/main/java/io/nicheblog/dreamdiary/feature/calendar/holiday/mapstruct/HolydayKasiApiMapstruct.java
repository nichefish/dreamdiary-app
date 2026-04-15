package io.nicheblog.dreamdiary.feature.calendar.holiday.mapstruct;

import io.nicheblog.dreamdiary.feature.calendar.schedule.entity.ScheduleEntity;
import io.nicheblog.dreamdiary.feature.calendar.holiday.model.HolydayKasiApiItemDto;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.cd.Code;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * HolydayKasiApiMapstruct
 * <pre>
 *  API:: 한국천문연구원(KASI):: 특일 정보 API Mapstruct 인터페이스
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = { Constant.class, Code.class, DateUtils.class, StringUtils.class })
public interface HolydayKasiApiMapstruct
        extends BaseWriteMapstruct<HolydayKasiApiItemDto, ScheduleEntity>, BaseMapstruct<HolydayKasiApiItemDto, ScheduleEntity> {

    HolydayKasiApiMapstruct INSTANCE = Mappers.getMapper(HolydayKasiApiMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    HolydayKasiApiItemDto toDto(final ScheduleEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Mapping(target = "src", expression = "java(\"KASI\")")
    @Mapping(target = "contentType", expression = "java(\"schedule\")")
    @Mapping(target = "title", expression = "java(dto.getDateName())")
    @Mapping(target = "cn", expression = "java(dto.getDateName())")
    @Mapping(target = "scheduleCd", expression = "java(Code.SCHEDULE_HOLYDAY)")
    @Mapping(target = "bgnDt", expression = "java(DateUtils.asDate(dto.getLocdate()))")
    @Mapping(target = "endDt", expression = "java(DateUtils.asDate(dto.getLocdate()))")
    ScheduleEntity toEntity(final HolydayKasiApiItemDto dto) throws Exception;

    /**
     * Entity update from Dto
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "title", expression = "java(dto.getDateName())")
    @Mapping(target = "scheduleCd", expression = "java(\"HOLYDAY\")")
    @Mapping(target = "bgnDt", expression = "java(DateUtils.asDate(dto.getLocdate()))")
    @Mapping(target = "endDt", expression = "java(DateUtils.asDate(dto.getLocdate()))")
    void updateFromDto(final HolydayKasiApiItemDto dto, final @MappingTarget ScheduleEntity entity) throws Exception;
}

