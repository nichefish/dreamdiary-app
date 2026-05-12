package io.nicheblog.dreamdiary.feature.journal.interpretation.mapstruct;

import io.nicheblog.dreamdiary.feature.attachable._shared.mapstruct.BaseAttachableMapstruct;
import io.nicheblog.dreamdiary.feature.journal.interpretation.entity.JournalInterpretationEntity;
import io.nicheblog.dreamdiary.feature.journal.interpretation.model.JournalInterpretationDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.code.utils.CodeUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;

/**
 * JournalInterpretationMapstruct
 * <pre>
 *  저널 해석 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = { DateUtils.class, StringUtils.class, DatePtn.class, MarkdownUtils.class, CodeUtils.class },
    builder = @Builder(disableBuilder = true)
)
public abstract class JournalInterpretationMapstruct
        implements BaseWriteMapstruct<JournalInterpretationDto, JournalInterpretationEntity>, BaseAttachableMapstruct<JournalInterpretationDto, JournalInterpretationEntity> {

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    public abstract JournalInterpretationEntity toEntity(final JournalInterpretationDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    public abstract void updateFromDto(final JournalInterpretationDto dto, final @MappingTarget JournalInterpretationEntity entity) throws Exception;

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "stdrdDt", expression = "java((entity.getJournalDay() != null && entity.getJournalDay().getJournalDate() != null) ? DateUtils.asStr(entity.getJournalDay().getJournalDate(), DatePtn.DATE) : null)")
    @Mapping(target = "journalDateWeekDay", expression = "java((entity.getJournalDay() != null && entity.getJournalDay().getJournalDate() != null) ? DateUtils.getDayOfWeekChinese(entity.getJournalDay().getJournalDate()) : null)")
    @Mapping(target = "yy", source = "journalDay.yy")
    @Mapping(target = "mnth", source = "journalDay.mnth")
    @Mapping(target = "markdownContent", expression = "java(StringUtils.isEmpty(entity.getContent()) ? \"-\" : MarkdownUtils.markdown(entity.getContent()))")
    public abstract JournalInterpretationDto toDto(final JournalInterpretationEntity entity) throws Exception;
}
