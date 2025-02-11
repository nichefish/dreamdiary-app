package io.nicheblog.dreamdiary.extension.log.sys.mapstruct;

import io.nicheblog.dreamdiary.extension.log.sys.entity.LogSysEntity;
import io.nicheblog.dreamdiary.extension.log.sys.model.LogSysDto;
import io.nicheblog.dreamdiary.extension.log.sys.model.LogSysParam;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseCrudMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * LogSysMapstruct
 * <pre>
 *  시스템 로그 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, DatePtn.class}, builder = @Builder(disableBuilder = true))
public interface LogSysMapstruct
        extends BaseCrudMapstruct<LogSysDto.DTL, LogSysDto.LIST, LogSysEntity> {

    LogSysMapstruct INSTANCE = Mappers.getMapper(LogSysMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toDto")
    @Mapping(target = "logDt", expression = "java(DateUtils.asStr(entity.getLogDt(), DatePtn.DATETIME))")
    LogSysDto.DTL toDto(final LogSysEntity entity) throws Exception;

    /**
     * Entity -> ListDto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return ListDto -- 변환된 ListDto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toListDto")
    @Mapping(target = "logDt", expression = "java(DateUtils.asStr(entity.getLogDt(), DatePtn.DATETIME))")
    LogSysDto.LIST toListDto(final LogSysEntity entity) throws Exception;

    /**
     * Param -> Entity 변환
     *
     * @param param 변환할 LogSysParam 객체
     * @return {@link LogSysEntity} -- 변환된 LogSysEntity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Mapping(target = "actvtyCtgrCd", expression = "java(param.getActvtyCtgr() != null ? param.getActvtyCtgr().name() : null)")
    LogSysEntity paramToEntity(final LogSysParam param) throws Exception;
}
