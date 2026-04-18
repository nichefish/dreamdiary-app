package io.nicheblog.dreamdiary.feature.admin.log.actvty.mapstruct;

import io.nicheblog.dreamdiary.feature.admin.log.actvty.model.LogActvtyQueryDto;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserRoleEntity;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.entity.LogActvtyEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * LogActvtyReadMapstruct
 * <pre>
 *  활동 로그 조회 매핑 전용 Mapper.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        imports = {DateUtils.class, StringUtils.class, DatePtn.class, CollectionUtils.class},
        builder = @Builder(disableBuilder = true)
)
public interface LogActvtyReadMapstruct
        extends BaseReadMapstruct<LogActvtyQueryDto, LogActvtyEntity> {

    LogActvtyReadMapstruct INSTANCE = Mappers.getMapper(LogActvtyReadMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "logDt", expression = "java(DateUtils.asStr(entity.getLogDt(), DatePtn.DATETIME))")
    @Mapping(target = "logUserNm", ignore = true)
    @Mapping(target = "roleKey", ignore = true)
    @Mapping(target = "roleName", ignore = true)
    LogActvtyQueryDto toDto(final LogActvtyEntity entity) throws Exception;

    /**
     * 작업자 표시명·첫 역할 (AuditorInfo.userRoles / role) 채움
     */
    @AfterMapping
    default void mapAuditorRoleFields(final LogActvtyEntity entity, final @MappingTarget LogActvtyQueryDto dto) {
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
