package io.nicheblog.dreamdiary.domain.flsys.mapstruct;

import io.nicheblog.dreamdiary.domain.flsys.entity.FlsysMetaEntity;
import io.nicheblog.dreamdiary.domain.flsys.model.FlsysMetaDto;
import io.nicheblog.dreamdiary.extension.cd.utils.CdUtils;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BasePostMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * FlsysMetaMapstruct
 * <pre>
 *  파일시스템 메타 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, CdUtils.class})
public interface FlsysMetaMapstruct
        extends BasePostMapstruct<FlsysMetaDto, FlsysMetaDto, FlsysMetaEntity> {

    FlsysMetaMapstruct INSTANCE = Mappers.getMapper(FlsysMetaMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toDto")
    @Mapping(target = "ctgrNm", expression = "java(CdUtils.getDtlCdNm(\"FLSYS_META_CTGR_CD\", entity.getCtgrCd()))")
    FlsysMetaDto toDto(final FlsysMetaEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    FlsysMetaEntity toEntity(final FlsysMetaDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final FlsysMetaDto dto, final @MappingTarget FlsysMetaEntity entity) throws Exception;
}
