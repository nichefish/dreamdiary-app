package io.nicheblog.dreamdiary.feature.admin.log.actvty.mapstruct;

import io.nicheblog.dreamdiary.feature.admin.log.actvty.model.LogActvtyQueryDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.entity.LogActvtyEntity;
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
        imports = {DateUtils.class, StringUtils.class, DatePtn.class},
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
    LogActvtyQueryDto toDto(final LogActvtyEntity entity) throws Exception;
}
