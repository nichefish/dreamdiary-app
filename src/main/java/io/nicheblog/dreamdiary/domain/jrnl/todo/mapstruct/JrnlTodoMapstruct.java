package io.nicheblog.dreamdiary.domain.jrnl.todo.mapstruct;

import io.nicheblog.dreamdiary.domain.jrnl.todo.entity.JrnlTodoEntity;
import io.nicheblog.dreamdiary.domain.jrnl.todo.model.JrnlTodoDto;
import io.nicheblog.dreamdiary.extension.cd.utils.CdUtils;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BasePostMapstruct;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * JrnlTodoMapstruct
 * <pre>
 *  저널 꿈 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, DatePtn.class, CmmUtils.class, CdUtils.class}, builder = @Builder(disableBuilder = true))
public interface JrnlTodoMapstruct
        extends BasePostMapstruct<JrnlTodoDto, JrnlTodoDto, JrnlTodoEntity> {

    JrnlTodoMapstruct INSTANCE = Mappers.getMapper(JrnlTodoMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toDto")
    @Mapping(target = "ctgrNm", expression = "java(CdUtils.getDtlCdNm(\"JRNL_TODO_CTGR_CD\", entity.getCtgrCd()))")
    @Mapping(target = "markdownCn", expression = "java(StringUtils.isEmpty(entity.getCn()) ? \"-\" : CmmUtils.markdown(entity.getCn()))")
    JrnlTodoDto toDto(final JrnlTodoEntity entity) throws Exception;

    /**
     * Entity -> ListDto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return ListDto -- 변환된 ListDto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toListDto")
    @Mapping(target = "ctgrNm", expression = "java(CdUtils.getDtlCdNm(\"JRNL_DREAM_CTGR_CD\", entity.getCtgrCd()))")
    @Mapping(target = "markdownCn", expression = "java(StringUtils.isEmpty(entity.getCn()) ? \"-\" : CmmUtils.markdown(entity.getCn()))")
    JrnlTodoDto toListDto(final JrnlTodoEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    JrnlTodoEntity toEntity(final JrnlTodoDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final JrnlTodoDto dto, final @MappingTarget JrnlTodoEntity entity) throws Exception;
}
