package io.nicheblog.dreamdiary.feature.journal.day.mapstruct;

import io.nicheblog.dreamdiary.feature.attachable._shared.mapstruct.BaseAttachableMapstruct;
import io.nicheblog.dreamdiary.feature.journal.chapter.mapstruct.JournalChapterMapstruct;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.entry.mapstruct.JournalEntryMapstruct;
import io.nicheblog.dreamdiary.feature.journal.entry.service.helper.JournalEntryViewProjectionHelper;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;

/**
 * JournalDayMapstruct
 * <pre>
 *  저널 일자 MapStruct Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = { DateUtils.class, DatePtn.class, StringUtils.class },
    uses = { JournalEntryMapstruct.class, JournalChapterMapstruct.class },
    builder = @Builder(disableBuilder = true)
)
public abstract class JournalDayMapstruct
        implements BaseWriteMapstruct<JournalDayDto, JournalDayEntity>, BaseAttachableMapstruct<JournalDayDto, JournalDayEntity> {

    /**
     * Dto를 Entity로 변환한다.
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Named("toEntity")
    @Mapping(target = "journalDate", expression = "java(DateUtils.asDate(dto.getJournalDate()))")
    @Mapping(target = "weekStartDt", expression = "java(DateUtils.asDate(dto.getWeekStartDt()))")
    public abstract JournalDayEntity toEntity(final JournalDayDto dto) throws Exception;

    /**
     * Dto 값으로 Entity를 갱신한다. Dto의 null 값은 Entity에 반영하지 않는다.
     *
     * @param dto 갱신할 값을 담은 Dto 객체
     * @param entity 갱신 대상 Entity 객체
     */
    @Override
    @Mapping(target = "journalDate", expression = "java(DateUtils.asDate(dto.getJournalDate()))")
    @Mapping(target = "weekStartDt", expression = "java(DateUtils.asDate(dto.getWeekStartDt()))")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateFromDto(final JournalDayDto dto, final @MappingTarget JournalDayEntity entity) throws Exception;

    /**
     * Entity를 Dto로 변환한다.
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "journalDate", expression = "java(DateUtils.asStr(entity.getJournalDate(), DatePtn.DATE))")
    @Mapping(target = "journalDateWeekDay", expression = "java(entity.getJournalDate() != null ? DateUtils.getDayOfWeekChinese(entity.getJournalDate()) : null)")
    @Mapping(target = "stdrdDt", expression = "java(DateUtils.asStr(entity.getJournalDate(), DatePtn.DATE))")
    @Mapping(target = "weekStartDt", expression = "java(DateUtils.asStr(entity.getWeekStartDt(), DatePtn.DATE))")
    @Mapping(target = "chapterList", source = "journalChapterList")
    public abstract JournalDayDto toDto(final JournalDayEntity entity) throws Exception;

    /**
     * toDto 후처리: DREAM 챕터의 꿈 목록을 JournalDayDto로 플래트닝한다.
     *
     * @param entity 변환 대상 엔티티
     * @param dto 반환될 Dto (MappingTarget)
     */
    @AfterMapping
    protected void flattenDreamLists(final JournalDayEntity entity, final @MappingTarget JournalDayDto dto) throws Exception {
        if (entity.getJournalChapterList() == null) return;
        JournalEntryViewProjectionHelper.applyDayDreamEntries(dto);
    }
}
