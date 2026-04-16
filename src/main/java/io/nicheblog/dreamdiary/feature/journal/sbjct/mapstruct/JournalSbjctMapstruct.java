package io.nicheblog.dreamdiary.feature.journal.sbjct.mapstruct;

import io.nicheblog.dreamdiary.feature.attachable._shared.mapstruct.BaseAttachableMapstruct;
import io.nicheblog.dreamdiary.feature.journal.sbjct.entity.JournalSbjctEntity;
import io.nicheblog.dreamdiary.feature.journal.sbjct.model.JournalSbjctDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.code.utils.CodeUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * JournalSbjctMapstruct
 * <pre>
 *  공지사항 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, MarkdownUtils.class, CodeUtils.class}, builder = @Builder(disableBuilder = true))
public interface JournalSbjctMapstruct
        extends BaseWriteMapstruct<JournalSbjctDto, JournalSbjctEntity>, BaseAttachableMapstruct<JournalSbjctDto, JournalSbjctEntity> {

    JournalSbjctMapstruct INSTANCE = Mappers.getMapper(JournalSbjctMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "ctgrNm", expression = "java(CodeUtils.getDtlCdNm(\"JOURNAL_SBJCT_CTGR_CD\", entity.getCtgrCd()))")
    @Mapping(target = "markdownContent", expression = "java(StringUtils.isEmpty(entity.getContent()) ? \"-\" : MarkdownUtils.markdown(entity.getContent()))")
    JournalSbjctDto toDto(final JournalSbjctEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    JournalSbjctEntity toEntity(final JournalSbjctDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    void updateFromDto(final JournalSbjctDto dto, final @MappingTarget JournalSbjctEntity entity) throws Exception;
}
