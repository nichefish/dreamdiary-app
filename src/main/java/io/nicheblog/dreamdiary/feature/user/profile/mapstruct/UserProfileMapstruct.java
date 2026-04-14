package io.nicheblog.dreamdiary.feature.user.profile.mapstruct;

import io.nicheblog.dreamdiary.feature.user.info.model.profile.UserProfileDto;
import io.nicheblog.dreamdiary.feature.user.profile.entity.UserProfileEntity;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, DatePtn.class})
public interface UserProfileMapstruct
        extends BaseWriteMapstruct<UserProfileDto, UserProfileEntity>, BaseMapstruct<UserProfileDto, UserProfileEntity> {

    UserProfileMapstruct INSTANCE = Mappers.getMapper(UserProfileMapstruct.class);

    @Override
    @Mapping(target = "brthdy", expression = "java(DateUtils.asStr(entity.getBrthdy(), DatePtn.DATE))")
    UserProfileDto toDto(final UserProfileEntity entity) throws Exception;

    @Override
    @Mapping(target = "brthdy", expression = "java(DateUtils.asDate(dto.getBrthdy()))")
    UserProfileEntity toEntity(final UserProfileDto dto) throws Exception;

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "brthdy", expression = "java(DateUtils.asDate(dto.getBrthdy()))")
    void updateFromDto(final UserProfileDto dto, final @MappingTarget UserProfileEntity entity) throws Exception;
}
