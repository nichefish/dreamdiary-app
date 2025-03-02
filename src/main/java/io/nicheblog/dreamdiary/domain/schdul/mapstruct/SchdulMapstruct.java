package io.nicheblog.dreamdiary.domain.schdul.mapstruct;

import io.nicheblog.dreamdiary.domain.schdul.entity.SchdulEntity;
import io.nicheblog.dreamdiary.domain.schdul.model.SchdulDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BasePostMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * SchdulMapstruct
 * <pre>
 *  일정 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, DatePtn.class})
public interface SchdulMapstruct
        extends BasePostMapstruct<SchdulDto, SchdulDto, SchdulEntity> {

    SchdulMapstruct INSTANCE = Mappers.getMapper(SchdulMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    // @Mapping(target = "prtcpnt", expression = "java(entity.getPrtcpntStr())")
    @Mapping(target = "bgnDt", expression = "java(DateUtils.asStr(entity.getBgnDt(), DatePtn.DATE))")
    @Mapping(target = "endDt", expression = "java(DateUtils.asStr(entity.getEndDt(), DatePtn.DATE))")
    // @Mapping(target = "prtcpntList", expression = "java(entity.getPrtcpntDtoList())")
    SchdulDto toDto(final SchdulEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Mapping(target = "bgnDt", expression = "java(DateUtils.asDate(dto.getBgnDt()))")
    @Mapping(target = "endDt", expression = "java(DateUtils.asDate(dto.getEndDt()))")
    SchdulEntity toEntity(final SchdulDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "bgnDt", expression = "java(DateUtils.asDate(dto.getBgnDt()))")
    @Mapping(target = "endDt", expression = "java(DateUtils.asDate(dto.getEndDt()))")
    void updateFromDto(final SchdulDto dto, final @MappingTarget SchdulEntity entity) throws Exception;
}
