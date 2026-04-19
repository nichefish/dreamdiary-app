package io.nicheblog.dreamdiary.feature.journal.note.mapstruct;

import io.nicheblog.dreamdiary.feature.attachable._shared.mapstruct.BaseAttachableMapstruct;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.day.type.JournalDatePrecision;
import io.nicheblog.dreamdiary.feature.journal.note.entity.JournalNoteEntity;
import io.nicheblog.dreamdiary.feature.journal.note.model.JournalNoteDto;
import io.nicheblog.dreamdiary.feature.journal.note.model.JournalNotePostDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.code.utils.CodeUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = { DateUtils.class, StringUtils.class, DatePtn.class, MarkdownUtils.class, CodeUtils.class, JournalDatePrecision.class },
    builder = @Builder(disableBuilder = true)
)
public abstract class JournalNoteMapstruct
        implements BaseWriteMapstruct<JournalNotePostDto, JournalNoteEntity>, BaseAttachableMapstruct<JournalNoteDto, JournalNoteEntity> {

    @PersistenceContext
    protected EntityManager em;

    @Override
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    @Mapping(target = "journalChapter", source = "journalChapterId", qualifiedByName = "mapJournalChapter")
    public abstract JournalNoteEntity toEntity(final JournalNotePostDto dto) throws Exception;

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    @Mapping(target = "journalChapter", source = "journalChapterId", qualifiedByName = "mapJournalChapter")
    public abstract void updateFromDto(final JournalNotePostDto dto, final @MappingTarget JournalNoteEntity entity) throws Exception;

    @Named("toDto")
    @Mapping(target = "journalChapterId", source = "journalChapter.id")
    @Mapping(target = "journalDayId", source = "journalChapter.journalDayId")
    @Mapping(target = "stdrdDt", expression = "java(entity.getJournalChapter().getJournalDay() != null ? DateUtils.asStr(entity.getJournalChapter().getJournalDay().getJournalDate(), DatePtn.DATE) : null)")
    @Mapping(target = "journalDatePrecision", expression = "java(entity.getJournalChapter().getJournalDay() != null ? entity.getJournalChapter().getJournalDay().getJournalDatePrecision() : JournalDatePrecision.EXACT)")
    @Mapping(target = "journalDateWeekDay", expression = "java(entity.getJournalChapter().getJournalDay() != null && entity.getJournalChapter().getJournalDay().getJournalDate() != null ? DateUtils.getDayOfWeekChinese(entity.getJournalChapter().getJournalDay().getJournalDate()) : null)")
    @Mapping(target = "yy", source = "journalChapter.journalDay.yy")
    @Mapping(target = "mnth", source = "journalChapter.journalDay.mnth")
    @Mapping(target = "markdownContent", expression = "java(StringUtils.isEmpty(entity.getContent()) ? \"-\" : MarkdownUtils.markdown(entity.getContent()))")
    public abstract JournalNoteDto toDto(final JournalNoteEntity entity) throws Exception;

    @Named("mapJournalChapter")
    protected JournalChapterEntity mapJournalChapter(final Integer journalChapterId) {
        if (journalChapterId == null) return null;
        return em.getReference(JournalChapterEntity.class, journalChapterId);
    }
}
