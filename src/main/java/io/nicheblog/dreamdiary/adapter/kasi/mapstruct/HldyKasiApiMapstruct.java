package io.nicheblog.dreamdiary.adapter.kasi.mapstruct;

import io.nicheblog.dreamdiary.adapter.kasi.model.HldyKasiApiItemDto;
import io.nicheblog.dreamdiary.domain.schdul.entity.SchdulEntity;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * HldyKasiApiMapstruct
 * <pre>
 *  API:: 한국천문연구원(KASI):: 특일 정보 API Mapstruct 인터페이스
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = { Constant.class, DateUtils.class, StringUtils.class })
public interface HldyKasiApiMapstruct
        extends BaseMapstruct<HldyKasiApiItemDto, SchdulEntity> {

    HldyKasiApiMapstruct INSTANCE = Mappers.getMapper(HldyKasiApiMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toDto")
    HldyKasiApiItemDto toDto(final SchdulEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Mapping(target = "src", expression = "java(\"KASI\")")
    @Mapping(target = "contentType", expression = "java(\"schdul\")")
    @Mapping(target = "title", expression = "java(dto.getDateName())")
    @Mapping(target = "cn", expression = "java(dto.getDateName())")
    @Mapping(target = "schdulCd", expression = "java(Constant.SCHDUL_HLDY)")
    @Mapping(target = "bgnDt", expression = "java(DateUtils.asDate(dto.getLocdate()))")
    @Mapping(target = "endDt", expression = "java(DateUtils.asDate(dto.getLocdate()))")
    SchdulEntity toEntity(final HldyKasiApiItemDto dto) throws Exception;

    /**
     * Entity update from Dto
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "title", expression = "java(dto.getDateName())")
    @Mapping(target = "schdulCd", expression = "java(\"HLDY\")")
    @Mapping(target = "bgnDt", expression = "java(DateUtils.asDate(dto.getLocdate()))")
    @Mapping(target = "endDt", expression = "java(DateUtils.asDate(dto.getLocdate()))")
    void updateFromDto(final HldyKasiApiItemDto dto, final @MappingTarget SchdulEntity entity) throws Exception;
}
