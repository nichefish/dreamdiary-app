package io.nicheblog.dreamdiary.extension.cd.mapstruct;

import io.nicheblog.dreamdiary.extension.cd.entity.ClCdEntity;
import io.nicheblog.dreamdiary.extension.cd.model.ClCdDto;
import io.nicheblog.dreamdiary.extension.cd.utils.CdUtils;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * ClCdMapstruct
 * <pre>
 *  분류 코드 관리 MapStruct 기반 Mapper 인터페이스.
 *  ※분류 코드(cl_cd) = 상위 분류 코드. 상세 코드(dtl_cd)를 1:N 묶음으로 관리한다.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {CdUtils.class})
public interface ClCdMapstruct
        extends BaseWriteMapstruct<ClCdDto, ClCdEntity>, BaseReadMapstruct<ClCdDto, ClCdEntity> {

    ClCdMapstruct INSTANCE = Mappers.getMapper(ClCdMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "clCtgrNm", expression = "java(CdUtils.getDtlCdNm(\"CL_CTGR_CD\", entity.getClCtgrCd()))")
    @Mapping(target = "dtlCdList", expression = "java(DtlCdMapstruct.INSTANCE.toDtoList(entity.getDtlCdList()))")
    ClCdDto toDto(final ClCdEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    ClCdEntity toEntity(final ClCdDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final ClCdDto dto, final @MappingTarget ClCdEntity entity) throws Exception;
}
