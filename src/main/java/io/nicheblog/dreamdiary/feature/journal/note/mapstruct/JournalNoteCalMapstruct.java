package io.nicheblog.dreamdiary.feature.journal.note.mapstruct;

import io.nicheblog.dreamdiary.feature.journal.note.model.JournalNoteCalDto;
import io.nicheblog.dreamdiary.feature.journal.note.model.JournalNoteDto;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * JournalNoteCalMapstruct
 * <pre>
 *  저널 일기 달력 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public interface JournalNoteCalMapstruct {

    JournalNoteCalMapstruct INSTANCE = Mappers.getMapper(JournalNoteCalMapstruct.class);

    /**
     * Dto -> CalDto 변환
     *
     * @param dto 변환할 Entity 객체
     * @return CalDto -- 변환된 달력 Dto 객체
     */
    @Named("toCalDto")
    @Mapping(target = "start", source = "stdrdDt")
    @Mapping(target = "end", source = "stdrdDt")
    JournalNoteCalDto toCalDto(final JournalNoteDto dto) throws Exception;
}
