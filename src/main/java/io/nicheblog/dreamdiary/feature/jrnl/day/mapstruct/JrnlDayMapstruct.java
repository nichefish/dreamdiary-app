package io.nicheblog.dreamdiary.feature.jrnl.day.mapstruct;

import io.nicheblog.dreamdiary.feature.clsf._shared.mapstruct.BaseClsfMapstruct;
import io.nicheblog.dreamdiary.feature.jrnl.day.entity.JrnlDayEntity;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.feature.jrnl.dream.mapstruct.JrnlDreamMapstruct;
import io.nicheblog.dreamdiary.feature.jrnl.entry.mapstruct.JrnlEntryMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;

/**
 * JrnlDayMapstruct
 * <pre>
 *  저널 일자 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = { DateUtils.class, DatePtn.class, StringUtils.class },
    uses = { JrnlDreamMapstruct.class, JrnlEntryMapstruct.class },
    builder = @Builder(disableBuilder = true)
)
public abstract class JrnlDayMapstruct
        implements BaseWriteMapstruct<JrnlDayDto, JrnlDayEntity>, BaseClsfMapstruct<JrnlDayDto, JrnlDayEntity> {

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Named("toEntity")
    @Mapping(target = "jrnlDt", expression = "java(DateUtils.asDate(dto.getJrnlDt()))")
    @Mapping(target = "aprxmtDt", expression = "java(DateUtils.asDate(dto.getAprxmtDt()))")
    @Mapping(target = "weekStartDt", expression = "java(DateUtils.asDate(dto.getWeekStartDt()))")
    public abstract JrnlDayEntity toEntity(final JrnlDayDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @Mapping(target = "jrnlDt", expression = "java(DateUtils.asDate(dto.getJrnlDt()))")
    @Mapping(target = "aprxmtDt", expression = "java(DateUtils.asDate(dto.getAprxmtDt()))")
    @Mapping(target = "weekStartDt", expression = "java(DateUtils.asDate(dto.getWeekStartDt()))")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateFromDto(final JrnlDayDto dto, final @MappingTarget JrnlDayEntity entity) throws Exception;

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "jrnlDt", expression = "java(DateUtils.asStr(entity.getJrnlDt(), DatePtn.DATE))")
    @Mapping(target = "jrnlDtWeekDay", expression = "java(entity.getJrnlDt() != null ? DateUtils.getDayOfWeekChinese(entity.getJrnlDt()) : null)")
    @Mapping(target = "aprxmtDt", expression = "java(DateUtils.asStr(entity.getAprxmtDt(), DatePtn.DATE))")
    @Mapping(target = "stdrdDt", expression = "java(DateUtils.asStr(\"Y\".equals(entity.getDtUnknownYn()) ? entity.getAprxmtDt() : entity.getJrnlDt(), DatePtn.DATE))")
    @Mapping(target = "weekStartDt", expression = "java(DateUtils.asStr(entity.getWeekStartDt(), DatePtn.DATE))")
    @Mapping(target = "entryList", source = "jrnlEntryList")
    public abstract JrnlDayDto toDto(final JrnlDayEntity entity) throws Exception;
}
