package io.nicheblog.dreamdiary.domain.jrnl.diary.mapstruct;

import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.extension.cd.utils.CdUtils;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BasePostMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * JrnlDiaryMapstruct
 * <pre>
 *  저널 일기 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, DatePtn.class, MarkdownUtils.class, CdUtils.class}, builder = @Builder(disableBuilder = true))
public interface JrnlDiaryMapstruct
        extends BasePostMapstruct<JrnlDiaryDto, JrnlDiaryDto, JrnlDiaryEntity> {

    JrnlDiaryMapstruct INSTANCE = Mappers.getMapper(JrnlDiaryMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "jrnlDayNo", expression = "java(entity.getJrnlEntry() != null ? entity.getJrnlEntry().getJrnlDayNo() : null)")
    @Mapping(target = "ctgrNm", expression = "java(CdUtils.getDtlCdNm(\"JRNL_DIARY_CTGR_CD\", entity.getCtgrCd()))")
    @Mapping(target = "stdrdDt", expression = "java(entity.getJrnlEntry().getJrnlDay() != null ? DateUtils.asStr(\"Y\".equals(entity.getJrnlEntry().getJrnlDay().getDtUnknownYn()) ? entity.getJrnlEntry().getJrnlDay().getAprxmtDt() : entity.getJrnlEntry().getJrnlDay().getJrnlDt(), DatePtn.DATE) : null)")
    @Mapping(target = "jrnlDtWeekDay", expression = "java(entity.getJrnlEntry().getJrnlDay() != null && entity.getJrnlEntry().getJrnlDay().getJrnlDt() != null ? DateUtils.getDayOfWeekChinese(entity.getJrnlEntry().getJrnlDay().getJrnlDt()) : null)")
    @Mapping(target = "yy", expression = "java(entity.getJrnlEntry() != null ? entity.getJrnlEntry().getJrnlDay().getYy() : null)")
    @Mapping(target = "mnth", expression = "java(entity.getJrnlEntry() != null ? entity.getJrnlEntry().getJrnlDay().getMnth() : null)")
    @Mapping(target = "markdownCn", expression = "java(StringUtils.isEmpty(entity.getCn()) ? \"-\" : MarkdownUtils.markdown(entity.getCn()))")
    JrnlDiaryDto toDto(final JrnlDiaryEntity entity) throws Exception;

    /**
     * Entity -> ListDto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return ListDto -- 변환된 ListDto 객체
     */
    @Override
    @Named("toListDto")
    @Mapping(target = "jrnlDayNo", expression = "java(entity.getJrnlEntry() != null ? entity.getJrnlEntry().getJrnlDayNo() : null)")
    @Mapping(target = "ctgrNm", expression = "java(CdUtils.getDtlCdNm(\"JRNL_DIARY_CTGR_CD\", entity.getCtgrCd()))")
    @Mapping(target = "stdrdDt", expression = "java(entity.getJrnlEntry().getJrnlDay() != null ? DateUtils.asStr(\"Y\".equals(entity.getJrnlEntry().getJrnlDay().getDtUnknownYn()) ? entity.getJrnlEntry().getJrnlDay().getAprxmtDt() : entity.getJrnlEntry().getJrnlDay().getJrnlDt(), DatePtn.DATE) : null)")
    @Mapping(target = "jrnlDtWeekDay", expression = "java(entity.getJrnlEntry().getJrnlDay() != null && entity.getJrnlEntry().getJrnlDay().getJrnlDt() != null ? DateUtils.getDayOfWeekChinese(entity.getJrnlEntry().getJrnlDay().getJrnlDt()) : null)")
    @Mapping(target = "yy", expression = "java(entity.getJrnlEntry() != null ? entity.getJrnlEntry().getJrnlDay().getYy() : null)")
    @Mapping(target = "mnth", expression = "java(entity.getJrnlEntry() != null ? entity.getJrnlEntry().getJrnlDay().getMnth() : null)")
    @Mapping(target = "markdownCn", expression = "java(StringUtils.isEmpty(entity.getCn()) ? \"-\" : MarkdownUtils.markdown(entity.getCn()))")
    JrnlDiaryDto toListDto(final JrnlDiaryEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Mapping(target = "cn", expression = "java(MarkdownUtils.normalize(dto.getCn()))")
    JrnlDiaryEntity toEntity(final JrnlDiaryDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "cn", expression = "java(MarkdownUtils.normalize(dto.getCn()))")
    void updateFromDto(final JrnlDiaryDto dto, final @MappingTarget JrnlDiaryEntity entity) throws Exception;
}
