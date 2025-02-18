package io.nicheblog.dreamdiary.domain.admin.lgnPolicy.mapstruct;

import io.nicheblog.dreamdiary.domain.admin.lgnPolicy.entity.LgnPolicyEntity;
import io.nicheblog.dreamdiary.domain.admin.lgnPolicy.model.LgnPolicyDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseMapstruct;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * LgnPolicyMapstruct
 * <pre>
 *  로그인 정책 관리 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LgnPolicyMapstruct
        extends BaseMapstruct<LgnPolicyDto, LgnPolicyEntity> {

    LgnPolicyMapstruct INSTANCE = Mappers.getMapper(LgnPolicyMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toDto")
    LgnPolicyDto toDto(final LgnPolicyEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    LgnPolicyEntity toEntity(final LgnPolicyDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final LgnPolicyDto dto, final @MappingTarget LgnPolicyEntity entity) throws Exception;
}
