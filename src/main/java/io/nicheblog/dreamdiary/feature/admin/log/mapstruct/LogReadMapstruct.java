package io.nicheblog.dreamdiary.feature.admin.log.mapstruct;

import io.nicheblog.dreamdiary.feature.admin.log.model.LogQueryDto;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserRoleEntity;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.log.entity.LogEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * 통합 로그 → 관리자 조회 DTO 매핑.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        imports = {DateUtils.class, DatePtn.class, CollectionUtils.class},
        builder = @Builder(disableBuilder = true)
)
public interface LogReadMapstruct
        extends BaseReadMapstruct<LogQueryDto, LogEntity> {

    LogReadMapstruct INSTANCE = Mappers.getMapper(LogReadMapstruct.class);

    @Override
    @Named("toDto")
    @Mapping(target = "logDt", expression = "java(DateUtils.asStr(entity.getCreatedAt(), DatePtn.DATETIME))")
    @Mapping(target = "actvtyCtgrCd", source = "activityCode")
    @Mapping(target = "actvtyCtgrNm", ignore = true)
    @Mapping(target = "ipAddr", source = "ipAddress")
    @Mapping(target = "param", source = "requestParam")
    @Mapping(target = "paramMap", expression = "java(entity.getParamMap())")
    @Mapping(target = "content", source = "message")
    @Mapping(target = "rsltMsg", source = "message")
    @Mapping(target = "rslt", expression = "java(entity.getResult() == null ? null : String.valueOf(entity.getResult()))")
    @Mapping(target = "exceptionNm", source = "exceptionName")
    @Mapping(target = "exceptionMsg", source = "exceptionMessage")
    @Mapping(target = "logUserNm", ignore = true)
    @Mapping(target = "roleKey", ignore = true)
    @Mapping(target = "roleName", ignore = true)
    LogQueryDto toDto(final LogEntity entity) throws Exception;

    @AfterMapping
    default void mapAuditorAndCategory(final LogEntity entity, final @MappingTarget LogQueryDto dto) {
        if (entity.getActivityCtgrInfo() != null) {
            dto.setActvtyCtgrNm(entity.getActivityCtgrInfo().getCodeName());
        }
        if (entity.getUserInfo() == null) {
            return;
        }
        dto.setLogUserNm(entity.getUserInfo().getNickname());
        if (CollectionUtils.isEmpty(entity.getUserInfo().getUserRoles())) {
            return;
        }
        final UserRoleEntity first = entity.getUserInfo().getUserRoles().get(0);
        if (first.getRoleInfo() != null) {
            dto.setRoleKey(first.getRoleInfo().getRoleKey());
            dto.setRoleName(first.getRoleInfo().getRoleName());
        } else {
            dto.setRoleKey(first.getRoleKey());
            dto.setRoleName(first.getRoleName());
        }
    }
}
