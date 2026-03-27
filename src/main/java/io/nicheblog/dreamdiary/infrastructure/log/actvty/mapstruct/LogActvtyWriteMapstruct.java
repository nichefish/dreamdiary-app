package io.nicheblog.dreamdiary.infrastructure.log.actvty.mapstruct;

import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.entity.LogActvtyEntity;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.model.LogActvtyParam;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * LogActvtyWriteMapstruct
 * <pre>
 *  활동 로그 적재 매핑 전용 Mapper.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface LogActvtyWriteMapstruct
        extends BaseWriteMapstruct<LogActvtyParam, LogActvtyEntity> {

    LogActvtyWriteMapstruct INSTANCE = Mappers.getMapper(LogActvtyWriteMapstruct.class);

    /**
     * Param -> Entity 변환
     *
     * @param dto 변환할 LogActvtyParam 객체
     * @return {@link LogActvtyEntity} -- 변환된 LogActvtyEntity 객체
     */
    @Override
    @Named("toEntity")
    @Mapping(target = "actvtyCtgrCd", expression = "java(dto.getActvtyCtgr() != null ? dto.getActvtyCtgr().name() : null)")
    LogActvtyEntity toEntity(final LogActvtyParam dto) throws Exception;
}
