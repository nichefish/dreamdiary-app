package io.nicheblog.dreamdiary.auth.policy.mapstruct;

import io.nicheblog.dreamdiary.auth.policy.entity.AuthPolicyEntity;
import io.nicheblog.dreamdiary.auth.policy.model.AuthPolicyDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * AuthPolicyMapstruct
 * <pre>
 *  인증 정책 관리 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthPolicyMapstruct
        extends BaseWriteMapstruct<AuthPolicyDto, AuthPolicyEntity>, BaseMapstruct<AuthPolicyDto, AuthPolicyEntity> {

    AuthPolicyMapstruct INSTANCE = Mappers.getMapper(AuthPolicyMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    AuthPolicyDto toDto(final AuthPolicyEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    AuthPolicyEntity toEntity(final AuthPolicyDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final AuthPolicyDto dto, final @MappingTarget AuthPolicyEntity entity) throws Exception;
}
