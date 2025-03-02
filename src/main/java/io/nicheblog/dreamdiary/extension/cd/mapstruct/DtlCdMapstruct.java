package io.nicheblog.dreamdiary.extension.cd.mapstruct;

import io.nicheblog.dreamdiary.extension.cd.entity.DtlCdEntity;
import io.nicheblog.dreamdiary.extension.cd.model.DtlCdDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseCrudMapstruct;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * DtlCdMapstruct
 * <pre>
 *  상세 코드 관리 MapStruct 기반 Mapper 인터페이스.
 *  ※상세 코드(dtl_cd) = 분류 코드 하위의 상세 코드. 분류 코드(cl_cd)에 N:1로 귀속된다.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DtlCdMapstruct
        extends BaseCrudMapstruct<DtlCdDto, DtlCdDto, DtlCdEntity> {

    DtlCdMapstruct INSTANCE = Mappers.getMapper(DtlCdMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toDto")
    DtlCdDto toDto(final DtlCdEntity entity) throws Exception;

    /**
     * Entity -> ListDto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return ListDto -- 변환된 ListDto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toListDto")
    DtlCdDto toListDto(final DtlCdEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    DtlCdEntity toEntity(final DtlCdDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final DtlCdDto dto, final @MappingTarget DtlCdEntity entity) throws Exception;
}
