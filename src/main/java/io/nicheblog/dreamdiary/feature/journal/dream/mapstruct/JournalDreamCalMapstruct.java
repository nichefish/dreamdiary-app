package io.nicheblog.dreamdiary.feature.journal.dream.mapstruct;

import io.nicheblog.dreamdiary.feature.journal.dream.model.JournalDreamCalDto;
import io.nicheblog.dreamdiary.feature.journal.dream.model.JournalDreamDto;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * JournalDreamCalMapstruct
 * <pre>
 *  저널 꿈 달력 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public interface JournalDreamCalMapstruct {

    JournalDreamCalMapstruct INSTANCE = Mappers.getMapper(JournalDreamCalMapstruct.class);

    /**
     * Dto -> CalDto 변환
     *
     * @param dto 변환할 Entity 객체
     * @return CalDto -- 변환된 달력 Dto 객체
     */
    @Named("toCalDto")
    @Mapping(target = "start", source = "stdrdDt")
    @Mapping(target = "end", source = "stdrdDt")
    JournalDreamCalDto toCalDto(final JournalDreamDto dto) throws Exception;
}
