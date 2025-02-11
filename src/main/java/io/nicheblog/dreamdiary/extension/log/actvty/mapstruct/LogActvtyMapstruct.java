package io.nicheblog.dreamdiary.extension.log.actvty.mapstruct;

import io.nicheblog.dreamdiary.extension.log.actvty.entity.LogActvtyEntity;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyDto;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseCrudMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * LogActvtyMapstruct
 * <pre>
 *  활동 로그 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, DatePtn.class}, builder = @Builder(disableBuilder = true))
public interface LogActvtyMapstruct
        extends BaseCrudMapstruct<LogActvtyDto.DTL, LogActvtyDto.LIST, LogActvtyEntity> {

    LogActvtyMapstruct INSTANCE = Mappers.getMapper(LogActvtyMapstruct.class);

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
    LogActvtyDto.DTL toDto(final LogActvtyEntity entity) throws Exception;

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
    LogActvtyDto.LIST toListDto(final LogActvtyEntity entity) throws Exception;

    /**
     * Param -> Entity 변환
     *
     * @param param 변환할 LogActvtyParam 객체
     * @return {@link LogActvtyEntity} -- 변환된 LogActvtyEntity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Mapping(target = "actvtyCtgrCd", expression = "java(param.getActvtyCtgr() != null ? param.getActvtyCtgr().name() : null)")
    LogActvtyEntity paramToEntity(final LogActvtyParam param) throws Exception;

}
