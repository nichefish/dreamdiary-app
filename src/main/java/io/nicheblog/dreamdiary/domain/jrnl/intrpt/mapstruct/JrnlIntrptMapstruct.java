package io.nicheblog.dreamdiary.domain.jrnl.intrpt.mapstruct;

import io.nicheblog.dreamdiary.domain.jrnl.intrpt.entity.JrnlIntrptEntity;
import io.nicheblog.dreamdiary.domain.jrnl.intrpt.model.JrnlIntrptDto;
import io.nicheblog.dreamdiary.extension.cd.utils.CdUtils;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BasePostMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * JrnlIntrptMapstruct
 * <pre>
 *  저널 해석 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, DatePtn.class, MarkdownUtils.class, CdUtils.class}, builder = @Builder(disableBuilder = true))
public interface JrnlIntrptMapstruct
        extends BasePostMapstruct<JrnlIntrptDto, JrnlIntrptDto, JrnlIntrptEntity> {

    JrnlIntrptMapstruct INSTANCE = Mappers.getMapper(JrnlIntrptMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "jrnlDayNo", expression = "java(entity.getJrnlDream() != null ? entity.getJrnlDream().getJrnlDayNo() : null)")
    @Mapping(target = "ctgrNm", expression = "java(CdUtils.getDtlCdNm(\"JRNL_DIARY_CTGR_CD\", entity.getCtgrCd()))")
    @Mapping(target = "stdrdDt", expression = "java(entity.getJrnlDream().getJrnlDay() != null ? DateUtils.asStr(\"Y\".equals(entity.getJrnlDream().getJrnlDay().getDtUnknownYn()) ? entity.getJrnlDream().getJrnlDay().getAprxmtDt() : entity.getJrnlDream().getJrnlDay().getJrnlDt(), DatePtn.DATE) : null)")
    @Mapping(target = "jrnlDtWeekDay", expression = "java(entity.getJrnlDream().getJrnlDay() != null && entity.getJrnlDream().getJrnlDay().getJrnlDt() != null ? DateUtils.getDayOfWeekChinese(entity.getJrnlDream().getJrnlDay().getJrnlDt()) : null)")
    @Mapping(target = "yy", expression = "java(entity.getJrnlDream() != null ? entity.getJrnlDream().getJrnlDay().getYy() : null)")
    @Mapping(target = "mnth", expression = "java(entity.getJrnlDream() != null ? entity.getJrnlDream().getJrnlDay().getMnth() : null)")
    @Mapping(target = "markdownCn", expression = "java(StringUtils.isEmpty(entity.getCn()) ? \"-\" : MarkdownUtils.markdown(entity.getCn()))")
    JrnlIntrptDto toDto(final JrnlIntrptEntity entity) throws Exception;

    /**
     * Entity -> ListDto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return ListDto -- 변환된 ListDto 객체
     */
    @Override
    @Named("toListDto")
    @Mapping(target = "jrnlDayNo", expression = "java(entity.getJrnlDream() != null ? entity.getJrnlDream().getJrnlDayNo() : null)")
    @Mapping(target = "ctgrNm", expression = "java(CdUtils.getDtlCdNm(\"JRNL_DIARY_CTGR_CD\", entity.getCtgrCd()))")
    @Mapping(target = "stdrdDt", expression = "java(entity.getJrnlDream().getJrnlDay() != null ? DateUtils.asStr(\"Y\".equals(entity.getJrnlDream().getJrnlDay().getDtUnknownYn()) ? entity.getJrnlDream().getJrnlDay().getAprxmtDt() : entity.getJrnlDream().getJrnlDay().getJrnlDt(), DatePtn.DATE) : null)")
    @Mapping(target = "jrnlDtWeekDay", expression = "java(entity.getJrnlDream().getJrnlDay() != null && entity.getJrnlDream().getJrnlDay().getJrnlDt() != null ? DateUtils.getDayOfWeekChinese(entity.getJrnlDream().getJrnlDay().getJrnlDt()) : null)")
    @Mapping(target = "yy", expression = "java(entity.getJrnlDream() != null ? entity.getJrnlDream().getJrnlDay().getYy() : null)")
    @Mapping(target = "mnth", expression = "java(entity.getJrnlDream() != null ? entity.getJrnlDream().getJrnlDay().getMnth() : null)")
    @Mapping(target = "markdownCn", expression = "java(StringUtils.isEmpty(entity.getCn()) ? \"-\" : MarkdownUtils.markdown(entity.getCn()))")
    JrnlIntrptDto toListDto(final JrnlIntrptEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Mapping(target = "cn", expression = "java(MarkdownUtils.normalize(dto.getCn()))")
    JrnlIntrptEntity toEntity(final JrnlIntrptDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "cn", expression = "java(MarkdownUtils.normalize(dto.getCn()))")
    void updateFromDto(final JrnlIntrptDto dto, final @MappingTarget JrnlIntrptEntity entity) throws Exception;
}
