package io.nicheblog.dreamdiary.infrastructure.log.sys.mapstruct;

import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.infrastructure.log.sys.entity.LogSysEntity;
import io.nicheblog.dreamdiary.infrastructure.log.sys.model.LogSysParam;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * LogSysWriteMapstruct
 * <pre>
 *  시스템 로그 적재 매핑 전용 Mapper.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface LogSysWriteMapstruct
        extends BaseWriteMapstruct<LogSysParam, LogSysEntity> {

    LogSysWriteMapstruct INSTANCE = Mappers.getMapper(LogSysWriteMapstruct.class);

    /**
     * Param -> Entity 변환
     *
     * @param dto 변환할 LogSysParam 객체
     * @return {@link LogSysEntity} -- 변환된 LogSysEntity 객체
     */
    @Override
    @Named("toEntity")
    @Mapping(target = "actvtyCtgrCd", expression = "java(dto.getActvtyCtgr() != null ? dto.getActvtyCtgr().name() : null)")
    LogSysEntity toEntity(final LogSysParam dto) throws Exception;
}
