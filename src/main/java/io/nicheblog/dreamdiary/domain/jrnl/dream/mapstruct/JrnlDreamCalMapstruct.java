package io.nicheblog.dreamdiary.domain.jrnl.dream.mapstruct;

import io.nicheblog.dreamdiary.domain.jrnl.dream.model.JrnlDreamCalDto;
import io.nicheblog.dreamdiary.domain.jrnl.dream.model.JrnlDreamDto;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * JrnlDreamCalMapstruct
 * <pre>
 *  저널 꿈 달력 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public interface JrnlDreamCalMapstruct {

    JrnlDreamCalMapstruct INSTANCE = Mappers.getMapper(JrnlDreamCalMapstruct.class);

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
    JrnlDreamCalDto toCalDto(final JrnlDreamDto dto) throws Exception;
}
