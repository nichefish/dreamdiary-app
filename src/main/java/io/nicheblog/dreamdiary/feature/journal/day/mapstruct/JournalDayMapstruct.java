package io.nicheblog.dreamdiary.feature.journal.day.mapstruct;

import io.nicheblog.dreamdiary.feature.clsf._shared.mapstruct.BaseClsfMapstruct;
import io.nicheblog.dreamdiary.feature.journal.chapter.mapstruct.JournalChapterMapstruct;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.dream.mapstruct.JournalDreamMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;

/**
 * JournalDayMapstruct
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
    uses = { JournalDreamMapstruct.class, JournalChapterMapstruct.class },
    builder = @Builder(disableBuilder = true)
)
public abstract class JournalDayMapstruct
        implements BaseWriteMapstruct<JournalDayDto, JournalDayEntity>, BaseClsfMapstruct<JournalDayDto, JournalDayEntity> {

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Named("toEntity")
    @Mapping(target = "journalDt", expression = "java(DateUtils.asDate(dto.getJournalDt()))")
    @Mapping(target = "aprxmtDt", expression = "java(DateUtils.asDate(dto.getAprxmtDt()))")
    @Mapping(target = "weekStartDt", expression = "java(DateUtils.asDate(dto.getWeekStartDt()))")
    public abstract JournalDayEntity toEntity(final JournalDayDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @Mapping(target = "journalDt", expression = "java(DateUtils.asDate(dto.getJournalDt()))")
    @Mapping(target = "aprxmtDt", expression = "java(DateUtils.asDate(dto.getAprxmtDt()))")
    @Mapping(target = "weekStartDt", expression = "java(DateUtils.asDate(dto.getWeekStartDt()))")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateFromDto(final JournalDayDto dto, final @MappingTarget JournalDayEntity entity) throws Exception;

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "journalDt", expression = "java(DateUtils.asStr(entity.getJournalDt(), DatePtn.DATE))")
    @Mapping(target = "journalDtWeekDay", expression = "java(entity.getJournalDt() != null ? DateUtils.getDayOfWeekChinese(entity.getJournalDt()) : null)")
    @Mapping(target = "aprxmtDt", expression = "java(DateUtils.asStr(entity.getAprxmtDt(), DatePtn.DATE))")
    @Mapping(target = "stdrdDt", expression = "java(DateUtils.asStr(\"Y\".equals(entity.getDtUnknownYn()) ? entity.getAprxmtDt() : entity.getJournalDt(), DatePtn.DATE))")
    @Mapping(target = "weekStartDt", expression = "java(DateUtils.asStr(entity.getWeekStartDt(), DatePtn.DATE))")
    @Mapping(target = "chapterList", source = "journalChapterList")
    public abstract JournalDayDto toDto(final JournalDayEntity entity) throws Exception;
}

