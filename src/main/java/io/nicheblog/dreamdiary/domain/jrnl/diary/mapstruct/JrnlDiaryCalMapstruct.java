package io.nicheblog.dreamdiary.domain.jrnl.diary.mapstruct;

import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiaryCalDto;
import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiaryDto;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * JrnlDiaryCalMapstruct
 * <pre>
 *  저널 일기 달력 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public interface JrnlDiaryCalMapstruct {

    JrnlDiaryCalMapstruct INSTANCE = Mappers.getMapper(JrnlDiaryCalMapstruct.class);

    /**
     * Dto -> CalDto 변환
     *
     * @param dto 변환할 Entity 객체
     * @return CalDto -- 변환된 달력 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Named("toCalDto")
    @Mapping(target = "id", source = "postNo")
    @Mapping(target = "start", source = "stdrdDt")
    @Mapping(target = "end", source = "stdrdDt")
    JrnlDiaryCalDto toCalDto(final JrnlDiaryDto dto) throws Exception;
}
