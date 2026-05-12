package io.nicheblog.dreamdiary.feature.journal.entry.mapstruct;

import io.nicheblog.dreamdiary.feature.attachable._shared.mapstruct.BaseAttachableMapstruct;
import io.nicheblog.dreamdiary.feature.journal.day.type.JournalDatePrecision;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryPostDto;
import io.nicheblog.dreamdiary.feature.journal.interpretation.mapstruct.JournalInterpretationMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.infrastructure.code.utils.CodeUtils;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = { MarkdownUtils.class, CodeUtils.class, JournalDatePrecision.class },
    uses = { JournalInterpretationMapstruct.class },
    builder = @Builder(disableBuilder = true)
)
public abstract class JournalEntryMapstruct
        implements BaseWriteMapstruct<JournalEntryPostDto, JournalEntryEntity>,
        BaseAttachableMapstruct<JournalEntryDto, JournalEntryEntity>,
        JournalEntryReadMapstructSupport {

    /**
     * 등록 DTO를 엔티티로 변환한다.
     *
     * @param dto 등록 DTO
     * @return 변환된 엔티티
     * @throws Exception 변환 중 예외
     */
    @Override
    @Mapping(target = "journalChapter", ignore = true)
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    public abstract JournalEntryEntity toEntity(final JournalEntryPostDto dto) throws Exception;

    /**
     * 수정 DTO 값을 기존 엔티티에 반영한다.
     *
     * @param dto 수정 DTO
     * @param entity 대상 엔티티
     * @throws Exception 변환 중 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "journalChapter", ignore = true)
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    public abstract void updateFromDto(final JournalEntryPostDto dto, final @MappingTarget JournalEntryEntity entity) throws Exception;

    /**
     * 엔티티를 상세 DTO로 변환한다.
     *
     * @param entity 원본 엔티티
     * @return 상세 DTO
     * @throws Exception 변환 중 예외
     */
    @Override
    public abstract JournalEntryDto toDto(final JournalEntryEntity entity) throws Exception;
}
