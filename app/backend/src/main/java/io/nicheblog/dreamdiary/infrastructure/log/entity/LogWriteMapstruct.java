package io.nicheblog.dreamdiary.infrastructure.log.entity;

import io.nicheblog.dreamdiary.infrastructure.log.model.LogParam;
import io.nicheblog.dreamdiary.infrastructure.log.type.LogType;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        imports = {LogType.class, LogMessageBuilder.class},
        builder = @Builder(disableBuilder = true)
)
public interface LogWriteMapstruct {

    LogWriteMapstruct INSTANCE = Mappers.getMapper(LogWriteMapstruct.class);

    @Named("toEntity")
    @Mapping(target = "activityCode", expression = "java(dto.getActvtyCtgr() != null ? dto.getActvtyCtgr().name() : null)")
    @Mapping(target = "message", expression = "java(LogMessageBuilder.messageBody(dto))")
    @Mapping(target = "result", source = "rslt")
    @Mapping(target = "ipAddress", source = "ipAddr")
    @Mapping(target = "requestParam", source = "param")
    @Mapping(target = "exceptionName", source = "exceptionNm")
    @Mapping(target = "exceptionMessage", source = "exceptionMsg")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userInfo", ignore = true)
    @Mapping(target = "activityCtgrInfo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    LogEntity toEntity(final LogParam dto) throws Exception;

    @Named("sysToEntity")
    @Mapping(target = "logType", expression = "java(LogType.SYSTEM)")
    @Mapping(target = "activityCode", expression = "java(dto.getActvtyCtgr() != null ? dto.getActvtyCtgr().name() : null)")
    @Mapping(target = "message", expression = "java(LogMessageBuilder.messageBody(dto))")
    @Mapping(target = "result", source = "rslt")
    @Mapping(target = "exceptionName", source = "exceptionNm")
    @Mapping(target = "exceptionMessage", source = "exceptionMsg")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userInfo", ignore = true)
    @Mapping(target = "activityCtgrInfo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "traceId", ignore = true)
    @Mapping(target = "httpMethod", ignore = true)
    @Mapping(target = "requestUri", ignore = true)
    @Mapping(target = "signature", ignore = true)
    @Mapping(target = "httpStatus", ignore = true)
    @Mapping(target = "durationMs", ignore = true)
    @Mapping(target = "requestParam", ignore = true)
    @Mapping(target = "referer", ignore = true)
    @Mapping(target = "ipAddress", ignore = true)
    LogEntity sysToEntity(final LogParam dto) throws Exception;
}
