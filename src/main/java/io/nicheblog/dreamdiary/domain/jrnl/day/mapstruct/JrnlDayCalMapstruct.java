package io.nicheblog.dreamdiary.domain.jrnl.day.mapstruct;

import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDayCalDto;
import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDayDto;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * JrnlDayCalMapstruct
 * <pre>
 *  저널 일자 달력 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public interface JrnlDayCalMapstruct {

    JrnlDayCalMapstruct INSTANCE = Mappers.getMapper(JrnlDayCalMapstruct.class);

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
    JrnlDayCalDto toCalDto(final JrnlDayDto dto) throws Exception;
}
