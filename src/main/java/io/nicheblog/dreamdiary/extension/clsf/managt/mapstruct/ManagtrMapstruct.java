package io.nicheblog.dreamdiary.extension.clsf.managt.mapstruct;

import io.nicheblog.dreamdiary.extension.clsf.managt.entity.ManagtrEntity;
import io.nicheblog.dreamdiary.extension.clsf.managt.model.ManagtrDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseCrudMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * ManagtrMapstruct
 * <pre>
 *  게시물 작업자 MapStruct 기반 Mapper 인터페이스.
 *  게시물 게시물 작업자(managtr) = 게시판 수정이력자. 게시판 게시물(board_post)에 1:N으로 귀속된다.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, DatePtn.class, StringUtils.class})
public interface ManagtrMapstruct
        extends BaseCrudMapstruct<ManagtrDto, ManagtrDto, ManagtrEntity> {

    ManagtrMapstruct INSTANCE = Mappers.getMapper(ManagtrMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toDto")
    @Mapping(target = "regDt", expression = "java(DateUtils.asStr(entity.getRegDt(), DatePtn.DATETIME))")
    @Mapping(target = "regstrNm", expression = "java(entity.getRegstrInfo() != null ? entity.getRegstrInfo().getNickNm() : null)")
    ManagtrDto toDto(final ManagtrEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    ManagtrEntity toEntity(final ManagtrDto dto) throws Exception;
}
