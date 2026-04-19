package io.nicheblog.dreamdiary.feature.journal.chapter.mapstruct;

import io.nicheblog.dreamdiary.feature.attachable._shared.mapstruct.BaseAttachableMapstruct;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterSmpDto;
import io.nicheblog.dreamdiary.feature.journal.diary.mapstruct.JournalDiaryMapstruct;
import io.nicheblog.dreamdiary.feature.journal.note.mapstruct.JournalNoteMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.code.utils.CodeUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JournalChapterMapstruct
 * <pre>
 *  저널 챕터 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = { DateUtils.class, StringUtils.class, DatePtn.class, MarkdownUtils.class, CodeUtils.class },
    uses = { JournalDiaryMapstruct.class, JournalNoteMapstruct.class },
    builder = @Builder(disableBuilder = true)
)
public abstract class JournalChapterMapstruct
        implements BaseWriteMapstruct<JournalChapterDto, JournalChapterEntity>, BaseAttachableMapstruct<JournalChapterDto, JournalChapterEntity> {

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "stdrdDt", expression = "java(entity.getJournalDay() != null ? DateUtils.asStr(entity.getJournalDay().getJournalDate(), DatePtn.DATE) : null)")
    @Mapping(target = "journalDateWeekDay", expression = "java(entity.getJournalDay() != null && entity.getJournalDay().getJournalDate() != null ? DateUtils.getDayOfWeekChinese(entity.getJournalDay().getJournalDate()) : null)")
    @Mapping(target = "yy", source = "journalDay.yy")
    @Mapping(target = "mnth", source = "journalDay.mnth")
    @Mapping(target = "categoryName", expression = "java(CodeUtils.getCodeName(\"JOURNAL_CHAPTER_CTGR_CD\", entity.getCategoryCode()))")
    public abstract JournalChapterDto toDto(final JournalChapterEntity entity) throws Exception;

    /**
     * Entity -> ListDto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return ListDto -- 변환된 ListDto 객체
     */
    @Named("toSmpDto")
    public abstract JournalChapterSmpDto toSmpDto(final JournalChapterEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    public abstract JournalChapterEntity toEntity(final JournalChapterDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateFromDto(final JournalChapterDto dto, final @MappingTarget JournalChapterEntity entity) throws Exception;

    /**
     * EntityList to DtoList
     *
     * @param entityList 변환할 Entity 목록
     * @return {@link List} -- 변환된 Dto 목록
     */
    public List<JournalChapterSmpDto> toSmpDtoList(final List<JournalChapterEntity> entityList) {
        if (CollectionUtils.isEmpty(entityList)) return null;
        return entityList.stream()
                .map(entity -> {
                    try {
                        return this.toSmpDto(entity);
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }
}

