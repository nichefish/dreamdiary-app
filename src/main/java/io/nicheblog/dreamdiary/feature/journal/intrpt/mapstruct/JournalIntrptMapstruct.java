package io.nicheblog.dreamdiary.feature.journal.intrpt.mapstruct;

import io.nicheblog.dreamdiary.feature.attachable._shared.mapstruct.BaseAttachableMapstruct;
import io.nicheblog.dreamdiary.feature.journal.dream.entity.JournalDreamEntity;
import io.nicheblog.dreamdiary.feature.journal.intrpt.entity.JournalIntrptEntity;
import io.nicheblog.dreamdiary.feature.journal.intrpt.model.JournalIntrptDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.code.utils.CodeUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * JournalIntrptMapstruct
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
public abstract class JournalIntrptMapstruct
        implements BaseWriteMapstruct<JournalIntrptDto, JournalIntrptEntity>, BaseAttachableMapstruct<JournalIntrptDto, JournalIntrptEntity> {

    @PersistenceContext
    protected EntityManager em;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    @Mapping(target = "journalDream", source = "journalDreamId", qualifiedByName = "mapJournalDream")
    public abstract JournalIntrptEntity toEntity(final JournalIntrptDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    @Mapping(target = "journalDream", source = "journalDreamId", qualifiedByName = "mapJournalDream")
    public abstract void updateFromDto(final JournalIntrptDto dto, final @MappingTarget JournalIntrptEntity entity) throws Exception;

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "journalDreamId", source = "journalDream.id")
    @Mapping(target = "journalDayId", source = "journalDream.journalDayId")
    @Mapping(target = "stdrdDt", expression = "java((entity.getJournalDream() != null && entity.getJournalDream().getJournalDay() != null) ? DateUtils.asStr(entity.getJournalDream().getJournalDay().getJournalDate(), DatePtn.DATE) : null)")
    @Mapping(target = "journalDateWeekDay", expression = "java((entity.getJournalDream() != null && entity.getJournalDream().getJournalDay() != null) && entity.getJournalDream().getJournalDay().getJournalDate() != null ? DateUtils.getDayOfWeekChinese(entity.getJournalDream().getJournalDay().getJournalDate()) : null)")
    @Mapping(target = "yy", source = "journalDream.journalDay.yy")
    @Mapping(target = "mnth", source = "journalDream.journalDay.mnth")
    @Mapping(target = "markdownContent", expression = "java(StringUtils.isEmpty(entity.getContent()) ? \"-\" : MarkdownUtils.markdown(entity.getContent()))")
    public abstract JournalIntrptDto toDto(final JournalIntrptEntity entity) throws Exception;

    /**
     * journalDreamId로부터 JournalDreamEntity 객체 생성
     * @param journalDreamId JournalDreamId
     * @return JournalDreamEntity
     */
    @Named("mapJournalDream")
    protected JournalDreamEntity mapJournalDream(final Integer journalDreamId) {
        if (journalDreamId == null) return null;
        return em.getReference(JournalDreamEntity.class, journalDreamId);
    }
}

