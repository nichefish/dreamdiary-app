package io.nicheblog.dreamdiary.feature.user.info.mapstruct;

import io.nicheblog.dreamdiary.feature.user.info.entity.UserAllowedIpEntity;
import io.nicheblog.dreamdiary.feature.user.info.model.UserAllowedIpDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * UserAllowedIpMapstruct
 * <pre>
 *  사용자 접속 IP MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, })
public interface UserAllowedIpMapstruct
        extends BaseWriteMapstruct<UserAllowedIpDto, UserAllowedIpEntity>, BaseMapstruct<UserAllowedIpDto, UserAllowedIpEntity> {

    UserAllowedIpMapstruct INSTANCE = Mappers.getMapper(UserAllowedIpMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    UserAllowedIpDto toDto(final UserAllowedIpEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    UserAllowedIpEntity toEntity(final UserAllowedIpDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final UserAllowedIpDto dto, final @MappingTarget UserAllowedIpEntity entity) throws Exception;
}
