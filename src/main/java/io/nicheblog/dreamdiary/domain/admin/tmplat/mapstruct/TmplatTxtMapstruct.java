package io.nicheblog.dreamdiary.domain.admin.tmplat.mapstruct;

import io.nicheblog.dreamdiary.domain.admin.tmplat.entity.TmplatTxtEntity;
import io.nicheblog.dreamdiary.domain.admin.tmplat.model.TmplatTxtDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseCrudMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * TmplatTxtMapstruct
 * <pre>
 *  템플릿 문구 정보 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, DatePtn.class})
public interface TmplatTxtMapstruct
        extends BaseCrudMapstruct<TmplatTxtDto, TmplatTxtDto, TmplatTxtEntity> {

    TmplatTxtMapstruct INSTANCE = Mappers.getMapper(TmplatTxtMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Mapping(target = "regDt", expression = "java(DateUtils.asStr(entity.getRegDt(), DatePtn.DATETIME))")
    TmplatTxtDto toDto(final TmplatTxtEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toEntity")
    TmplatTxtEntity toEntity(final TmplatTxtDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final TmplatTxtDto dto, final @MappingTarget TmplatTxtEntity entity) throws Exception;
}
