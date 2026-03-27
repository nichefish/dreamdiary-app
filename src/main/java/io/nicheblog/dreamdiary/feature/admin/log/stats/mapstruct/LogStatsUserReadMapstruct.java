package io.nicheblog.dreamdiary.feature.admin.log.stats.mapstruct;

import io.nicheblog.dreamdiary.feature.admin.log.stats.model.LogStatsUserQueryDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.log.stats.entity.LogStatsUserEntity;
import io.nicheblog.dreamdiary.infrastructure.log.stats.model.LogStatsUserIntrfc;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * LogStatsUserReadMapstruct
 * <pre>
 *  사용자 로그 통계 조회 매핑 전용 Mapper.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        imports = {DateUtils.class, StringUtils.class}
)
public interface LogStatsUserReadMapstruct
        extends BaseMapstruct<LogStatsUserQueryDto, LogStatsUserEntity> {

    LogStatsUserReadMapstruct INSTANCE = Mappers.getMapper(LogStatsUserReadMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    LogStatsUserQueryDto toDto(final LogStatsUserEntity entity) throws Exception;

    /**
     * Interface -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    LogStatsUserQueryDto toDto(final LogStatsUserIntrfc entity) throws Exception;
}
