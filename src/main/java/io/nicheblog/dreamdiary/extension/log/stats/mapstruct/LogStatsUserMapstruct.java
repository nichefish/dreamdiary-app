package io.nicheblog.dreamdiary.extension.log.stats.mapstruct;

import io.nicheblog.dreamdiary.extension.log.stats.entity.LogStatsUserEntity;
import io.nicheblog.dreamdiary.extension.log.stats.model.LogStatsUserDto;
import io.nicheblog.dreamdiary.extension.log.stats.model.LogStatsUserIntrfc;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * LogActvtyMapstruct
 * <pre>
 *  사용자별 로그 통계 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class})
public interface LogStatsUserMapstruct
        extends BaseMapstruct<LogStatsUserDto, LogStatsUserEntity> {

    LogStatsUserMapstruct INSTANCE = Mappers.getMapper(LogStatsUserMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    LogStatsUserDto toDto(final LogStatsUserEntity entity) throws Exception;

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    LogStatsUserDto toDto(final LogStatsUserIntrfc entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toEntity")
    LogStatsUserEntity toEntity(final LogStatsUserDto dto) throws Exception;
}
