package io.nicheblog.dreamdiary.feature.journal.diary.mapstruct;

import io.nicheblog.dreamdiary.feature.clsf._shared.mapstruct.BaseClsfMapstruct;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.diary.entity.JournalDiaryEntity;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryDto;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryPostDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.cd.utils.CdUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * JournalDiaryMapstruct
 * <pre>
 *  저널 일기 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = { DateUtils.class, StringUtils.class, DatePtn.class, MarkdownUtils.class, CdUtils.class },
    builder = @Builder(disableBuilder = true)
)
public abstract class JournalDiaryMapstruct
        implements BaseWriteMapstruct<JournalDiaryPostDto, JournalDiaryEntity>, BaseClsfMapstruct<JournalDiaryDto, JournalDiaryEntity> {

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
    @Mapping(target = "journalChapter", source = "journalChapterId", qualifiedByName = "mapJournalChapter")
    public abstract JournalDiaryEntity toEntity(final JournalDiaryPostDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    @Mapping(target = "journalChapter", source = "journalChapterId", qualifiedByName = "mapJournalChapter")
    public abstract void updateFromDto(final JournalDiaryPostDto dto, final @MappingTarget JournalDiaryEntity entity) throws Exception;

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Named("toDto")
    @Mapping(target = "journalChapterId", source = "journalChapter.id")
    @Mapping(target = "journalDayId", source = "journalChapter.journalDayId")
    @Mapping(target = "stdrdDt", expression = "java(entity.getJournalChapter().getJournalDay() != null ? DateUtils.asStr(\"Y\".equals(entity.getJournalChapter().getJournalDay().getDtUnknownYn()) ? entity.getJournalChapter().getJournalDay().getAprxmtDt() : entity.getJournalChapter().getJournalDay().getJournalDt(), DatePtn.DATE) : null)")
    @Mapping(target = "dtUnknownYn", expression = "java(entity.getJournalChapter().getJournalDay() != null ? entity.getJournalChapter().getJournalDay().getDtUnknownYn() : \"N\")")
    @Mapping(target = "journalDtWeekDay", expression = "java(entity.getJournalChapter().getJournalDay() != null && entity.getJournalChapter().getJournalDay().getJournalDt() != null ? DateUtils.getDayOfWeekChinese(entity.getJournalChapter().getJournalDay().getJournalDt()) : null)")
    @Mapping(target = "yy", source = "journalChapter.journalDay.yy")
    @Mapping(target = "mnth", source = "journalChapter.journalDay.mnth")
    @Mapping(target = "markdownContent", expression = "java(StringUtils.isEmpty(entity.getContent()) ? \"-\" : MarkdownUtils.markdown(entity.getContent()))")
    public abstract JournalDiaryDto toDto(final JournalDiaryEntity entity) throws Exception;

    /**
     * journalChapterId로부터 JournalChapterEntity 객체 생성
     * @param journalChapterId journalChapterId
     * @return JournalChapterEntity
     */
    @Named("mapJournalChapter")
    protected JournalChapterEntity mapJournalChapter(final Integer journalChapterId) {
        if (journalChapterId == null) return null;
        return em.getReference(JournalChapterEntity.class, journalChapterId);
    }
}

