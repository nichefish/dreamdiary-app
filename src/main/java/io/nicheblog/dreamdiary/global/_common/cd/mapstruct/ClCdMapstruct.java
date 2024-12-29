package io.nicheblog.dreamdiary.global._common.cd.mapstruct;

import io.nicheblog.dreamdiary.global._common.cd.entity.ClCdEntity;
import io.nicheblog.dreamdiary.global._common.cd.model.ClCdDto;
import io.nicheblog.dreamdiary.global._common.cd.utils.CdUtils;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseCrudMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
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
        extends BaseCrudMapstruct<ClCdDto, ClCdDto, ClCdEntity> {

    ClCdMapstruct INSTANCE = Mappers.getMapper(ClCdMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toDto")
    @Mapping(target = "clCtgrNm", expression = "java(CdUtils.getDtlCdNm(\"CL_CTGR_CD\", entity.getClCtgrCd()))")
    @Mapping(target = "dtlCdList", expression = "java(DtlCdMapstruct.INSTANCE.toDtoList(entity.getDtlCdList()))")
    ClCdDto toDto(final ClCdEntity entity) throws Exception;

    /**
     * Entity -> ListDto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return ListDto -- 변환된 ListDto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toListDto")
    @Mapping(target = "clCtgrNm", expression = "java(CdUtils.getDtlCdNm(\"CL_CTGR_CD\", entity.getClCtgrCd()))")
    ClCdDto toListDto(final ClCdEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    ClCdEntity toEntity(final ClCdDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 DTO 객체
     * @param entity 업데이트할 대상 엔티티 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final ClCdDto dto, final @MappingTarget ClCdEntity entity) throws Exception;
}
