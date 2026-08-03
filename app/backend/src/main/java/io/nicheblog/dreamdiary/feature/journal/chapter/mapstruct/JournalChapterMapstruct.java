package io.nicheblog.dreamdiary.feature.journal.chapter.mapstruct;

import io.nicheblog.dreamdiary.feature.attachable._shared.mapstruct.BaseAttachableMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterSmpDto;
import io.nicheblog.dreamdiary.feature.journal.entry.mapstruct.JournalEntryMapstruct;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.service.helper.JournalEntryViewProjectionHelper;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JournalChapterMapstruct
 * JournalChapter 엔티티와 DTO 간 MapStruct 매핑을 담당한다.
 *
 * @author nichefish
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = { DateUtils.class, StringUtils.class, DatePtn.class, MarkdownUtils.class },
    uses = { JournalEntryMapstruct.class },
    builder = @Builder(disableBuilder = true)
)
public abstract class JournalChapterMapstruct
        implements BaseWriteMapstruct<JournalChapterDto, JournalChapterEntity>, BaseAttachableMapstruct<JournalChapterDto, JournalChapterEntity> {

    @Autowired
    protected JournalEntryMapstruct journalEntryMapstruct;

    @Override
    @Named("toDto")
    @Mapping(target = "stdrdDt", expression = "java(entity.getJournalDay() != null ? DateUtils.asStr(entity.getJournalDay().getJournalDate(), DatePtn.DATE) : null)")
    @Mapping(target = "journalDateWeekDay", expression = "java(entity.getJournalDay() != null && entity.getJournalDay().getJournalDate() != null ? DateUtils.getDayOfWeekChinese(entity.getJournalDay().getJournalDate()) : null)")
    @Mapping(target = "yy", source = "journalDay.yy")
    @Mapping(target = "mnth", source = "journalDay.mnth")
    @Mapping(target = "prefix", expression = "java(selectedPrefixToDto(entity))")
    @Mapping(target = "prefixId", expression = "java(entity.getSelectedPrefixId())")
    public abstract JournalChapterDto toDto(final JournalChapterEntity entity) throws Exception;

    @Named("toSmpDto")
    @Mapping(target = "prefix", expression = "java(selectedPrefixToDto(entity))")
    @Mapping(target = "prefixId", expression = "java(entity.getSelectedPrefixId())")
    public abstract JournalChapterSmpDto toSmpDto(final JournalChapterEntity entity) throws Exception;

    @Override
    @Mapping(target = "prefix", ignore = true)
    public abstract JournalChapterEntity toEntity(final JournalChapterDto dto) throws Exception;

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "prefix", ignore = true)
    public abstract void updateFromDto(final JournalChapterDto dto, final @MappingTarget JournalChapterEntity entity) throws Exception;

    /**
     * 챕터가 선택한 말머리(prefix_content 연결)를 표시용 DTO로 변환한다.
     *
     * @param entity 저널 챕터 엔티티
     * @return 선택 말머리 DTO. 선택이 없으면 {@code null}
     */
    protected PrefixDto selectedPrefixToDto(final JournalChapterEntity entity) {
        final PrefixEntity prefix = entity.getSelectedPrefix();
        if (prefix == null) return null;
        return PrefixDto.builder()
                .id(prefix.getId())
                .name(prefix.getName())
                .color(prefix.getColor())
                .sortOrder(prefix.getSortOrder())
                .activeYn(prefix.getActiveYn())
                .build();
    }

    @AfterMapping
    protected void mapEntryLists(final JournalChapterEntity entity, final @MappingTarget JournalChapterDto dto) throws Exception {
        final List<JournalEntryDto> journalEntryList = CollectionUtils.isEmpty(entity.getJournalEntryList())
                ? null
                : journalEntryMapstruct.toDtoList(entity.getJournalEntryList());
        JournalEntryViewProjectionHelper.applyChapterEntries(dto, journalEntryList);
    }

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
