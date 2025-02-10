package io.nicheblog.dreamdiary.domain.jrnl.dream.mapstruct;

import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamEntity;
import io.nicheblog.dreamdiary.domain.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.extension.cd.utils.CdUtils;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BasePostMapstruct;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * JrnlDreamMapstruct
 * <pre>
 *  저널 꿈 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, DatePtn.class, CmmUtils.class, CdUtils.class}, builder = @Builder(disableBuilder = true))
public interface JrnlDreamMapstruct
        extends BasePostMapstruct<JrnlDreamDto, JrnlDreamDto, JrnlDreamEntity> {

    JrnlDreamMapstruct INSTANCE = Mappers.getMapper(JrnlDreamMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toDto")
    @Mapping(target = "ctgrNm", expression = "java(CdUtils.getDtlCdNm(\"JRNL_DREAM_CTGR_CD\", entity.getCtgrCd()))")
    @Mapping(target = "stdrdDt", expression = "java(entity.getJrnlDay() != null ? DateUtils.asStr(\"Y\".equals(entity.getJrnlDay().getDtUnknownYn()) ? entity.getJrnlDay().getAprxmtDt() : entity.getJrnlDay().getJrnlDt(), DatePtn.DATE) : null)")
    @Mapping(target = "yy", expression = "java(entity.getJrnlDay() != null ? entity.getJrnlDay().getYy() : null)")
    @Mapping(target = "mnth", expression = "java(entity.getJrnlDay() != null ? entity.getJrnlDay().getMnth() : null)")
    @Mapping(target = "markdownCn", expression = "java(StringUtils.isEmpty(entity.getCn()) ? \"-\" : CmmUtils.markdown(entity.getCn()))")
    JrnlDreamDto toDto(final JrnlDreamEntity entity) throws Exception;

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
    @Mapping(target = "stdrdDt", expression = "java(entity.getJrnlDay() != null ? DateUtils.asStr(\"Y\".equals(entity.getJrnlDay().getDtUnknownYn()) ? entity.getJrnlDay().getAprxmtDt() : entity.getJrnlDay().getJrnlDt(), DatePtn.DATE) : null)")
    @Mapping(target = "yy", expression = "java(entity.getJrnlDay() != null ? entity.getJrnlDay().getYy() : null)")
    @Mapping(target = "mnth", expression = "java(entity.getJrnlDay() != null ? entity.getJrnlDay().getMnth() : null)")
    @Mapping(target = "markdownCn", expression = "java(StringUtils.isEmpty(entity.getCn()) ? \"-\" : CmmUtils.markdown(entity.getCn()))")
    JrnlDreamDto toListDto(final JrnlDreamEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    JrnlDreamEntity toEntity(final JrnlDreamDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 DTO 객체
     * @param entity 업데이트할 대상 엔티티 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final JrnlDreamDto dto, final @MappingTarget JrnlDreamEntity entity) throws Exception;
}
