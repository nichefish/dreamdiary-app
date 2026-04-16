package io.nicheblog.dreamdiary.feature.journal.dream.mapstruct;

import io.nicheblog.dreamdiary.feature.attachable._shared.mapstruct.BaseAttachableMapstruct;
import io.nicheblog.dreamdiary.feature.journal.dream.entity.JournalDreamEntity;
import io.nicheblog.dreamdiary.feature.journal.dream.model.JournalDreamDto;
import io.nicheblog.dreamdiary.feature.journal.dream.model.JournalDreamPostDto;
import io.nicheblog.dreamdiary.feature.journal.intrpt.mapstruct.JournalIntrptMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.code.utils.CodeUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;

/**
 * JournalDreamMapstruct
 * <pre>
 *  저널 꿈 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = { DateUtils.class, StringUtils.class, DatePtn.class, MarkdownUtils.class, CodeUtils.class },
    uses = { JournalIntrptMapstruct.class },
    builder = @Builder(disableBuilder = true)
)
public abstract class JournalDreamMapstruct
        implements BaseWriteMapstruct<JournalDreamPostDto, JournalDreamEntity>, BaseAttachableMapstruct<JournalDreamDto, JournalDreamEntity> {

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    public abstract JournalDreamEntity toEntity(final JournalDreamPostDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    public abstract void updateFromDto(final JournalDreamPostDto dto, final @MappingTarget JournalDreamEntity entity) throws Exception;

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Mapping(target = "stdrdDt", expression = "java(entity.getJournalDay() != null ? DateUtils.asStr(\"Y\".equals(entity.getJournalDay().getDtUnknownYn()) ? entity.getJournalDay().getAprxmtDt() : entity.getJournalDay().getJournalDt(), DatePtn.DATE) : null)")
    @Mapping(target = "dtUnknownYn", expression = "java(entity.getJournalDay() != null ? entity.getJournalDay().getDtUnknownYn() : \"N\")")
    @Mapping(target = "journalDtWeekDay", expression = "java(entity.getJournalDay() != null && entity.getJournalDay().getJournalDt() != null ? DateUtils.getDayOfWeekChinese(entity.getJournalDay().getJournalDt()) : null)")
    @Mapping(target = "yy", source = "journalDay.yy")
    @Mapping(target = "mnth", source = "journalDay.mnth")
    @Mapping(target = "markdownContent", expression = "java(StringUtils.isEmpty(entity.getContent()) ? \"-\" : MarkdownUtils.markdown(entity.getContent()))")
    public abstract JournalDreamDto toDto(final JournalDreamEntity entity) throws Exception;
}

