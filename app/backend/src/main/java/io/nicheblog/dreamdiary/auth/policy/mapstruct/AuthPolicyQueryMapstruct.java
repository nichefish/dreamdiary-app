package io.nicheblog.dreamdiary.auth.policy.mapstruct;

import io.nicheblog.dreamdiary.auth.policy.entity.AuthPolicyEntity;
import io.nicheblog.dreamdiary.auth.policy.model.AuthPolicyQueryDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * AuthPolicyQueryMapstruct
 * <pre>
 *  인증 정책 조회 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthPolicyQueryMapstruct
        extends BaseReadMapstruct<AuthPolicyQueryDto, AuthPolicyEntity> {

    AuthPolicyQueryMapstruct INSTANCE = Mappers.getMapper(AuthPolicyQueryMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    AuthPolicyQueryDto toDto(final AuthPolicyEntity entity) throws Exception;
}
