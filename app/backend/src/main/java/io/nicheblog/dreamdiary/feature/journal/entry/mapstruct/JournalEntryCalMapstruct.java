package io.nicheblog.dreamdiary.feature.journal.entry.mapstruct;

import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryCalDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public interface JournalEntryCalMapstruct {

    JournalEntryCalMapstruct INSTANCE = Mappers.getMapper(JournalEntryCalMapstruct.class);

    /**
     * 엔트리 DTO를 캘린더 DTO로 변환한다.
     *
     * @param dto 원본 엔트리 DTO
     * @return 캘린더 DTO
     * @throws Exception 변환 중 예외
     */
    @Named("toCalDto")
    @Mapping(target = "start", source = "stdrdDt")
    @Mapping(target = "end", source = "stdrdDt")
    JournalEntryCalDto toCalDto(final JournalEntryDto dto) throws Exception;
}
