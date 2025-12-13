package io.nicheblog.dreamdiary.auth.security.mapstruct;

import io.nicheblog.dreamdiary.auth.security.entity.AuthRoleEntity;
import io.nicheblog.dreamdiary.auth.security.model.AuthRoleDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * AuthRoleMapstruct
 * <pre>
 *  권한 정보 관리 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class})
public interface AuthRoleMapstruct
        extends BaseWriteMapstruct<AuthRoleDto, AuthRoleEntity>, BaseReadMapstruct<AuthRoleDto, AuthRoleEntity> {

    AuthRoleMapstruct INSTANCE = Mappers.getMapper(AuthRoleMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    AuthRoleDto toDto(final AuthRoleEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    AuthRoleEntity toEntity(final AuthRoleDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final AuthRoleDto dto, final @MappingTarget AuthRoleEntity entity) throws Exception;
}
