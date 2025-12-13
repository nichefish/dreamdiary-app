package io.nicheblog.dreamdiary.domain.jrnl.sumry.mapstruct;

import io.nicheblog.dreamdiary.domain.jrnl.sumry.entity.JrnlSumryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.model.JrnlSumryDto;
import io.nicheblog.dreamdiary.extension.cd.utils.CdUtils;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseClsfMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * JrnlSumryMapstruct
 * <pre>
 *  저널 결산 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, DatePtn.class, StringUtils.class, MarkdownUtils.class, CdUtils.class}, builder = @Builder(disableBuilder = true))
public interface JrnlSumryMapstruct
        extends BaseWriteMapstruct<JrnlSumryDto, JrnlSumryEntity>, BaseClsfMapstruct<JrnlSumryDto, JrnlSumryEntity> {

    JrnlSumryMapstruct INSTANCE = Mappers.getMapper(JrnlSumryMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "ctgrNm", expression = "java(CdUtils.getDtlCdNm(\"JRNL_SUMRY_CTGR_CD\", entity.getCtgrCd()))")
    @Mapping(target = "markdownCn", expression = "java(StringUtils.isEmpty(entity.getCn()) ? \"-\" : MarkdownUtils.markdown(entity.getCn()))")
    JrnlSumryDto toDto(final JrnlSumryEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Mapping(target = "cn", expression = "java(MarkdownUtils.normalize(dto.getCn()))")
    JrnlSumryEntity toEntity(final JrnlSumryDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "cn", expression = "java(MarkdownUtils.normalize(dto.getCn()))")
    void updateFromDto(final JrnlSumryDto dto, final @MappingTarget JrnlSumryEntity entity) throws Exception;
}
